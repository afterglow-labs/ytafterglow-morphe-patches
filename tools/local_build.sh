#!/usr/bin/env bash
# Builds the patch bundle locally, without the Gradle plugin or GitHub Packages.
#
# - Kotlin patches: compiled with Android Studio's bundled kotlinc against the
#   morphe-patcher 2.1.0 classes inside Morphe Desktop's all-in-one jar.
# - Extension: javac + d8 from the Android SDK, saved as extensions/afterglow.mpe
#   (an .mpe is the extension's classes.dex, renamed).
# - Bundle: a jar holding the JVM patch classes (Morphe Desktop), their DEX
#   (Morphe Manager on Android) and the extension, with the same manifest
#   attributes the Gradle plugin writes, including Patcher-Version 2.1.0.
#
# Output: build-local/YTAfterglow-<version>-local.mpp
# Override any tool path with the environment variables below.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ANDROID_HOME="${ANDROID_HOME:-$HOME/Android/Sdk}"
KOTLINC="${KOTLINC:-/mnt/c/Program Files/Android/Android Studio/plugins/Kotlin/kotlinc/bin/kotlinc}"
DESKTOP_JAR="${MORPHE_DESKTOP_JAR:-/mnt/c/Users/Corey/Downloads/morphe-desktop-1.20.0-all.jar}"
ANDROID_JAR="${ANDROID_JAR:-$ANDROID_HOME/platforms/android-36/android.jar}"
D8="${D8:-$ANDROID_HOME/build-tools/37.0.0/d8}"
JDK="${JDK:-/usr/lib/jvm/java-17-openjdk-amd64}"
OUT="${OUT:-$ROOT/build-local}"
PATCHER_VERSION="2.1.0"

for tool in "$KOTLINC" "$DESKTOP_JAR" "$ANDROID_JAR" "$D8" "$JDK/bin/javac" "$JDK/bin/jar"; do
    [[ -e "$tool" ]] || { echo "missing: $tool" >&2; exit 1; }
done

VERSION="$(sed -n 's/^version *= *//p' "$ROOT/gradle.properties")"
rm -rf "$OUT"
mkdir -p "$OUT/ext-classes" "$OUT/ext-dex" "$OUT/patch-classes" "$OUT/patch-dex" "$OUT/bundle/extensions"

echo "==> extension: javac"
mapfile -t JAVA_SOURCES < <(find "$ROOT/extensions/extension/src/main/java" -name '*.java' | sort)
"$JDK/bin/javac" -encoding UTF-8 -source 17 -target 17 -nowarn -Xlint:none \
    -classpath "$ANDROID_JAR" -d "$OUT/ext-classes" "${JAVA_SOURCES[@]}"

echo "==> extension: d8"
mapfile -t EXT_CLASSES < <(find "$OUT/ext-classes" -name '*.class' | sort)
JAVA_HOME="$JDK" "$D8" --release --min-api 29 --lib "$ANDROID_JAR" --output "$OUT/ext-dex" "${EXT_CLASSES[@]}"
[[ ! -e "$OUT/ext-dex/classes2.dex" ]] || { echo "extension needs multidex; not supported here" >&2; exit 1; }
cp "$OUT/ext-dex/classes.dex" "$OUT/bundle/extensions/afterglow.mpe"

echo "==> patches: kotlinc"
mapfile -t KOTLIN_SOURCES < <(find "$ROOT/patches/src/main/kotlin/app" -name '*.kt' | sort)
JAVA_HOME="$JDK" "$KOTLINC" -jvm-target 11 -no-reflect -nowarn -Xskip-prerelease-check -Xcontext-parameters \
    -classpath "$DESKTOP_JAR" -d "$OUT/patch-classes" "${KOTLIN_SOURCES[@]}"

echo "==> patches: d8"
mapfile -t PATCH_CLASSES < <(find "$OUT/patch-classes" -name '*.class' | sort)
JAVA_HOME="$JDK" "$D8" --release --min-api 26 --lib "$ANDROID_JAR" --classpath "$DESKTOP_JAR" \
    --output "$OUT/patch-dex" "${PATCH_CLASSES[@]}"

echo "==> bundle"
cp -r "$OUT/patch-classes/." "$OUT/bundle/"
if [[ -d "$ROOT/patches/src/main/resources" ]]; then
    cp -r "$ROOT/patches/src/main/resources/." "$OUT/bundle/"
fi
rm -rf "$OUT/bundle/META-INF"
cp "$OUT/patch-dex/classes.dex" "$OUT/bundle/classes.dex"
cat > "$OUT/MANIFEST.MF" <<EOF
Manifest-Version: 1.0
Name: YTAfterglow
Description: YTAfterglow for YouTube on Android: every Afterglow tweak, setting and screen from the iOS tweak.
Version: $VERSION
Timestamp: $(date +%s000)
Source: git@github.com:afterglow-labs/ytafterglow-morphe-patches.git
Author: afterglow-labs
Contact: https://github.com/afterglow-labs/ytafterglow-morphe-patches/issues
Website: https://github.com/afterglow-labs/ytafterglow-morphe-patches
License: GPLv3
Patcher-Version: $PATCHER_VERSION
EOF
MPP="$OUT/YTAfterglow-$VERSION-local.mpp"
"$JDK/bin/jar" --create --file "$MPP" --manifest "$OUT/MANIFEST.MF" -C "$OUT/bundle" .
echo "==> built $MPP"

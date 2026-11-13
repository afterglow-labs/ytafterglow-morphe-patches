# Handoff

> Updated: 2026-11-13, native themes and Theme Studio presets
> Project: ytafterglow-morphe-patches (active checkout `/home/corey/projects/ytafterglow-morphe-patches`; original checkout `/home/corey/projects/xuninc/YTAfterglow-Android`, GitHub `afterglow-labs/ytafterglow-morphe-patches`, private)
> Session summary: The shared theme engine now has a native YouTube resource/Litho adapter and a working Theme Studio preset screen. There are 18 named tweaks, with parity at 56 done / 4 n/a / 139 todo. Theme Studio remains partial: editor/library, native gradients, glow and typography are still pending.

## Goal

Port every YTAfterglow iOS tweak to Android YouTube **21.43.523**, exactly as it behaves on iOS: same settings, defaults, screens, translations and theme/glow look. Deliver it as the user's own standalone Morphe patch source, a `.mpp` bundle that Morphe Desktop or Manager loads next to Morphe's own list.
- Each Afterglow feature group is a separately named tweak in Morphe's list (names from `tools/tweaks.json`).
- An Afterglow settings page lives inside YouTube's settings and shows only rows for tweaks that are installed and implemented.
- Plumbing such as GmsCore/microG sign-in comes from Morphe's own "GmsCore support" patch, ticked in the same run.

## Native theme checkpoint — 2026-11-13

The active checkout is still `/home/corey/projects/ytafterglow-morphe-patches`, on `main`.
The **Theme Studio** named patch adds the 30 iOS presets, active identity, swatches, seasonal
group and reset. The editor, saved library and theme-file transfer remain hidden. Native
colors require Android 14+; the screen explains that requirement on older devices.

The native adapter isolates 74 style bindings in 55 aliases and maps 38 additional semantic
resources (93 total). It attaches an overlay to base contexts and the main activity, hooks
the verified Litho background drawable, and selects YouTube's LIGHT/DARK foundation without
changing the saved Appearance setting. Reset returns to that saved setting. Shared white,
black and brand-red constants remain untouched. See [`docs/native-themes.md`](docs/native-themes.md).

Testing exposed two old/new integration boundaries: the 2.1 resource encoder requires the
canonical `colors.xml` / `overlayable.xml` filenames; and restarting in the same process left
the user on Home. The unexported `RestartActivity` now relaunches from a short-lived separate
process after preference writes are flushed. Removing `finishAffinity()` alone did not solve it.

Current totals: **18 named patches; 56 done / 4 n/a / 139 todo**. Theme Studio remains partial.
292 theme-engine and 67 settings-visibility checks pass. Strict `--validate` application
passes 18/18 standalone and 19/19 with official GmsCore. The final candidate is `build-08`
under `/mnt/c/Users/Corey/ytag-scratch/android/native-theme-20261113-01/`.

The signed combined APK was installed on the Android 16 phone emulator. The actual settings
row opens Theme Studio; Warm Sand and Midnight Blue show the expected native foundations and
catalog background colors. Applying a preset now reopens YouTube automatically. Theme Studio
reset removes the native overlay and restores the original appearance. Runtime logs contain
no Afterglow errors or fatal exception, and the crash buffer is empty. Full details/screenshots
are in the native-themes document and evidence directory above. Both reset entry points were
exercised; the emulator was restored to system colors and shut down after testing.

Remaining themes work: native gradient attachment, fuller navigation/icon/player coverage,
exact semantic tinting for server-driven text, custom editor/library and import/export,
then Glow and Typography. No physical-device or video-playback proof is claimed.

## Current State

**Baseline at takeover (`750b016`; see the checkpoint below for subsequent work):**
- **17 named tweaks**, all applied in a strict Morphe Desktop 1.20.0 run on YouTube 21.43.523: Background playback, Playback speed, Skip content warning, Video quality, Captions, Autoplay, Shorts layout and buttons, Shorts, Player buttons and menus, Watch page, Comments header, Remove ads, Navigation bar, Feed menus, Tab bar, Startup tab and animation, Privacy.
- **Parity:** 55 done, 4 n/a, 140 todo of 199 iOS keys (`tools/parity.json`).
- **Afterglow settings page:** all 24 iOS pages, 346 rows, 21 pickers, 225 search entries, tab editor, iOS-style glowing two-tone wordmark row. Its signature orange/white constants follow current iOS Settings.x; surrounding surfaces read theme keys.
- **Base pieces:**
  - extension init hook (`MainActivity.onCreate`);
  - Litho component filter (`litho/`);
  - menu-row hook (`menus/`);
  - video-state base (`video/VideoState`: id, Shorts, state, time, seek, speed, listeners; for SponsorBlock and others);
  - `InstalledTweaks` gating.
- **Emulator-verified** (Android 16 phone image, patched with Morphe `patches-2.0.0.mpp` "GmsCore support" plus ours, microG installed):
  - the Shorts hides (Like, Comments, Share, Remix, channel name, description);
  - hiding Shorts in search;
  - the settings row and pages.

**Not done / unverified:**
- **No real-device test.** The user's S25 Ultra and Z Fold7 weren't reachable over ADB or Tailscale.
- **The glow look** has never been visually compared on screen with the iOS reference screenshots.
- **Partial keys:** noPromotionCards, noSubbar, noPlayerShareButton/SaveButton (the on-video View parts), autoplayMode "off", shortsOnlyMode, commentsHeaderMode "pinned".
- **Not started**, biggest first:
  - complete native theme coverage and Theme Studio editor/library; Glow and Typography (shared palette, native resource/Litho adapter and preset picker now exist);
  - Control Center (queued, never started);
  - two-row tab bar, side anchors, tab composition from `TabStore` (Messages `FEconnections_inbox`, Afterglow tab);
  - SponsorBlock (Afterglow's own client, incl. Shorts);
  - overlay buttons (mute, lock, download, Premium controls sheet, clean-overlay eye);
  - player gestures and fullscreen, seek bar, end time;
  - Downloads/Vault, AI Home, AB Lab, Lite Mode, long-press tools, classicQuality, rememberCaptionState, rememberLoop, miniplayer, noRelatedWatchNexts.

## Takeover checkpoint — 2026-11-12

The active checkout is `/home/corey/projects/ytafterglow-morphe-patches`, on `main`, remote
`afterglow-labs/ytafterglow-morphe-patches`. The original Android-named checkout is separate.
The user asked Codex to take over; the selected next batch is the theme engine.

The shared **color foundation** is implemented: 20 semantic roles, all 30 iOS master presets,
derived colors, correct iOS light/dark foundation selection, cached immutable snapshots, and
atomic preset/reset APIs. Existing Afterglow settings use that palette. See
[`docs/theme-engine.md`](docs/theme-engine.md) for exact scope, verification and the next boundary.

This does not finish native YouTube theming, Theme Studio, Glow or Typography. No new unfinished
controls are exposed, and parity remains 55 done / 4 n/a / 140 todo, with 17 named patches.
The theme engine JVM suite passes 252 checks; the existing settings visibility suite passes 65.
The local bundle builds. Strict patch validation passes standalone (17/17) and with official GmsCore support (18/18). The signed combined APK installed on the Android 16 phone emulator. Afterglow settings passed a cold launch and imported `theme_background=#909090`, showing the expected light foundation with dark text. Restoring defaults and cold reopening returned to the original appearance. The crash buffer was empty and captured logs contained no Afterglow errors. No physical-device test was performed. The emulator was shut down after testing.
Artifacts/logs: `/mnt/c/Users/Corey/ytag-scratch/android/theme-engine-20261112-01/`.

Strict fingerprint validation found a baseline player-status enum collision with our own merged
extension. The fingerprint now excludes the extension namespace. Cast visibility matching now tolerates the two instructions inserted by the official GmsCore dependency and captures the match before editing. Both configurations pass with `--validate`; keep that gate enabled.

## Files in Play

| File | Why It Matters |
|------|---------------|
| `tools/tweaks.json` | Tweak id → Morphe name + owned iOS keys (`base` keys always show). |
| `tools/parity.json` + `tools/check_parity.py` | Status per iOS key (`done` needs `where`, `n/a` needs `why`). |
| `tools/check_tweaks.py`, `tools/check_settings_ui.py` (+ `tools/jvm/SettingsVisibilityTest.java`) | Every key owned once; every named patch calls `markTweakInstalled`; settings spec plus visibility rules. |
| `tools/generate_from_ios.py <ios repo>` | Registry (`settings/Registry.java`) + 13 languages (`i18n/`) from iOS `YTAGPreferenceDescriptor.m` and `Localizable.strings`. |
| `tools/generate_settings_ui.py`, `tools/ios-settings-ui.json` | iOS settings layout → generated `ui/SettingsSpec.java`. |
| `tools/generate_tweaks.py` | `tweaks.json` + parity → generated `settings/TweakCatalog.java` (`SHOWN_KEYS`). |
| `tools/local_build.sh` | Offline build → `build-local/YTAfterglow-<ver>-local.mpp`. |
| `patches/src/main/kotlin/app/afterglow/patches/TweakPatches.kt` | The first 8 named tweaks. Other batches live in `tweaks/*Tweaks.kt`. |
| `patches/.../shared/{Constants,AfterglowExtensionPatch,Tweaks,BytecodeHelpers}.kt` | Compatibility (21.43.523), extension init, `markTweakInstalled(id)`. |
| `patches/.../settings/AfterglowSettingsPatch.kt` | Adds the exported `AfterglowActivity` with an action intent-filter, the row layout/icon resources, and the row in `res/xml/settings_fragment_cairo.xml`. |
| `extensions/extension/src/main/java/app/afterglow/extension/` | `Afterglow`, `InstalledTweaks`, `settings/` (AfterglowPrefs, TabStore, LiteMode, PrefsTransfer), `ui/`, `litho/`, `menus/`, `video/`, `playback/`, `navigation/`. |
| `/mnt/c/Users/Corey/ytag-scratch/morphe-official-v2.2.0/` | Official Morphe source, the reference for hooks. Written for patcher **2.3.0**; translate to 2.1.0. |
| `/mnt/c/Users/Corey/ytag-scratch/morphe-research/featuremap.md` + `morphe-feasibility-report.md` | iOS key ↔ Morphe patch line-up. "Afterglow only" means not in Morphe. |
| `/mnt/c/Users/Corey/ytag-scratch/android/yt-21.43.523/dump/classes*.txt` | `dexdump -d` of 21.43.523. Search with `rg -F` (about 2 s). |
| `/mnt/c/Users/Corey/ytag-scratch/android/ios-reference/` | Current iOS screenshots: settings row glow, Control Center (IMG_3190–3198), Theme Studio (IMG_3199–3201). |
| iOS source (read-only spec) `/home/corey/projects/xuninc/YTAfterglow/.worktrees/yt-21.43-hook-repair` | Behavior spec: `YTAfterglow.x`, `Settings.x`, `ColorMode.x`, `Utils/YTAGThemeStore.m`, `Utils/YTAGThemeCatalog.m`, `UI/*`. |

## What Has Been Tried (and Why It Failed)

### Official add-on API / fork framing
- **What:** first proposed plugging into Morphe's `AddOnManager`/`AddOnApi`, or forking.
- **Why it was rejected:** the user wants their own standalone tweak repo ("I want MY tweaks / MY experience"). It's neither a fork nor the add-on contract.

### One umbrella "YTAfterglow" patch
- **What:** a single named patch, with features toggled only in-app.
- **Why it was rejected:** the user wants a tweak list in Morphe. It was split into named tweaks per `tweaks.json`.

### Rebuilding GmsCore support
- **What:** started our own GmsCore patch.
- **Why it was dropped:** use Morphe's own GmsCore patch, ticked alongside ours. Our code must be package-name independent.

### Settings row: `targetPackage` intent, then unexported action intent
- **What:** a fixed package broke once GmsCore renamed the package. An implicit intent to an unexported activity was blocked on Android 14+ (`ActivityNotFoundException`).
- **Fix:** exported activity with an action intent-filter (committed).

### Gradle build via GitHub Packages
- **What:** the normal Gradle build.
- **Why it failed:** the gh token lacks `read:packages`. Use `tools/local_build.sh` instead.

### jadx decompiler
- **What:** download and run jadx.
- **Why it failed:** blocked by the auto-mode classifier. Use the SDK's `dexdump`/`aapt2`.

### Plain `invoke-static {p0}` in big methods
- **What:** a plain invoke in `MainActivity.onCreate`.
- **Why it failed:** smali error "maximum allowed register … v15". Use `invoke-static/range {p0 .. p0}`.

### `AfterglowActivity` styling bars before `setContentView`
- **What:** styled the system bars before the content view existed.
- **Why it failed:** NPE on API 36. Fixed: bars are styled after the content is set.

### Hard-coding colors from screenshots
- **What:** sampled colors from iOS screenshots.
- **Why it was wrong:** the user said all colors come from the selected theme. Port the iOS derivation and fallbacks.

### Calling features Morphe has when they're Afterglow-only
- **What:** the research labels made it look as if Morphe had Vault, Theme Studio, glow and the 11-tab bar.
- **Why it was wrong:** Morphe has none of them. The notes are fixed; never describe these as Morphe features.

## Current Best Theory

- **Adapt the official code for overlapping features.** For tweaks Morphe also has (about 130 overlap), adapt the official Morphe implementation in `morphe-official-v2.2.0`: its fingerprints already match 21.43.523. Rewire it to our iOS keys plus `InstalledTweaks`, under `app.afterglow.*`, with GPL attribution headers and never the name "Morphe".
- **Build Afterglow-only features from the iOS source.**
- **Theme engine first.** It unblocks the settings look, glow, Theme Studio and the Control Center gradient.
  - The official theme patch uses resource variants via `mcc`/`mnc` qualifiers, FabricatedOverlay (Android 14+), and Litho color hooks, which need a restart.
  - Afterglow needs per-role colors from the `theme_*` keys.

## Next Steps

1. **Continue themes:** finish native gradient/navigation/icon coverage and the Theme Studio editor/library. The shared palette, resource/Litho adapter, foundation selection and preset picker now exist. Do not re-ask which batch; the takeover already selected themes.
2. **For a batch:**
   - Read the iOS implementation.
   - Find the Morphe equivalent in `featuremap.md`.
   - Verify every fingerprint against the dexdump.
   - Add named patches in `patches/.../tweaks/<Area>Tweaks.kt`: `bytecodePatch(name=…, description=…, default=true)`, `compatibleWith(COMPATIBILITY_YOUTUBE)`, `dependsOn(base patches)`, and `markTweakInstalled("<id>")` in `execute`.
   - Gate runtime on `InstalledTweaks.has(id)` plus the key (via `LiteMode.effectiveBool`/`AfterglowPrefs`). Fail open with `Afterglow.error`.
   - If the tweak id already has a named patch in `TweakPatches.kt`, add an unnamed hook patch and append it to that patch's `dependsOn`.
3. **Build and check:**
   `OUT=/mnt/c/Users/Corey/ytag-scratch/android/build bash tools/local_build.sh && python3 -B -I tools/check_parity.py && python3 -B -I tools/check_tweaks.py && python3 -B -I tools/check_settings_ui.py`
4. **Patch-test (JDK 21):** Prefer `tools/verify_bundle.py` (see `docs/theme-engine.md`) for exclusive patch selection, `--validate`, a saved command and a checked JSON result. The direct CLI equivalent begins:
   `/usr/lib/jvm/java-21-openjdk-amd64/bin/java -Xmx6g -jar /mnt/c/Users/Corey/Downloads/morphe-desktop-1.20.0-all.jar patch -p <mpp> --unsigned -o /mnt/c/Users/Corey/ytag-scratch/android/test/out.apk -t /mnt/c/Users/Corey/ytag-scratch/android/test/tmp "/mnt/c/Users/Corey/Downloads/com.google.android.youtube_21.43.523-1561307650_minAPI29(arm64-v8a,armeabi-v7a,x86,x86_64)(nodpi)_apkmirror.com.apk"`
   Every patch must log "Applied". Then update `tools/parity.json`, run `python3 -B -I tools/generate_tweaks.py`, and commit and push to main.
5. **Emulator run:**
   - Patch using `tools/verify_bundle.py` with `--gms-bundle /mnt/c/Users/Corey/Downloads/patches-2.0.0.mpp`. Merely adding `-e "GmsCore support"` without exclusive selection also enables unrelated default patches.
   - Sign: `apksigner sign --ks /mnt/c/Users/Corey/ytag-scratch/android/signing/ytag-test.jks --ks-key-alias ytagtest --ks-pass pass:ytagtest123 --v1-signing-enabled false --v2-signing-enabled true --v3-signing-enabled true`.
   - Verified phone emulator: Windows SDK `emulator.exe`, AVD `YTAG_Glow_Check`, `ANDROID_AVD_HOME=C:\Users\Corey\ytag-scratch\android\ui-test\avd`, flags `-no-window -no-audio -no-snapshot -gpu swiftshader_indirect -port 5600`. When launching from WSL, include `ANDROID_AVD_HOME` in `WSLENV`. Use Windows SDK `platform-tools/adb.exe` for `emulator-5600`. The Linux SDK phone-image path in the older handoff was not the actual working setup.
   - Install microG `Downloads/microg-7.3.0.apk` and the signed APK.
   - Screenshots via `adb emu screenrecord screenshot <dir>`. Shut it down afterwards (`adb emu kill`).
6. **Real device:** ask the user for the S25 Ultra's wireless-debugging IP:port, or to bring it online on Tailscale (`coreys-s25-ultra`, 100.85.166.64).

## Key Constraints

- **The spec is the iOS code, ported 1:1.** Don't ask vision or priority questions. Only floatingKeyboard and stockVolumeHUD are truly iOS-only. disableFloatingTabBar and hideShortsLogo are n/a because Android 21.43 has neither element.
- **Patcher 2.1.0, old API** (`bytecodePatch`/`resourcePatch`, `Fingerprint(...)` named args). Check signatures with `javap -classpath /mnt/c/Users/Corey/Downloads/morphe-desktop-1.20.0-all.jar <class>`. kotlinc needs `-Xcontext-parameters`. The bundle loads in the user's current Desktop 1.20.0 and Manager, and in newer hosts.
- **Push straight to main, no PRs.** The repo is private, so Manager can't add it by URL; deliver the `.mpp` as a local file.
- **Never use `app.morphe.*` packages or "Morphe" in names.** Keep GPL attribution headers on adapted code.
- **The Linux root disk fills up** (other projects). Put build outputs on C: (`/mnt/c/Users/Corey/ytag-scratch/...`). Never delete other projects' files.
- **Installed tools are the verified build path:** JDK, Android SDK, Android Studio kotlinc and Morphe Desktop jar. Earlier download/tool restrictions were session-specific; do not treat them as a permanent environment limitation.
- **Follow the current iOS source for colors and glow.** App surface/control colors derive from the active theme. The settings wordmark has dedicated signature constants in Settings.x; do not recolor those based on a general statement in an older handoff.

## Memory Pointers

- Memory files under `~/.claude/projects/-home-corey-projects-xuninc-YTAfterglow/memory/`: `morphe-port-research`, `morphe-use-their-plumbing`, `existing-tweaks-are-the-spec`, `push-to-main-not-prs`.
- Searches worth re-running: `ytafterglow-morphe-patches parity`, `Afterglow Litho filter 21.43.523`, `Morphe GmsCore emulator Shorts test`.

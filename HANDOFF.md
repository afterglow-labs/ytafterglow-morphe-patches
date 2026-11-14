# Handoff

> Updated: 2026-11-14, live native action-row refresh
> Project: ytafterglow-morphe-patches (active checkout `/home/corey/projects/ytafterglow-morphe-patches`; original checkout `/home/corey/projects/xuninc/YTAfterglow-Android`, GitHub `afterglow-labs/ytafterglow-morphe-patches`, private)
> Session summary: Theme Studio includes the editor, saved library and theme-file transfer. Native gradients, glow, bundled fonts, custom scrubbers, tap-to-seek, ambient-mode control and nine player overlay hide switches work in the Android 16 emulator. SponsorBlock skip/undo and timeline markers also work in watch playback and Shorts. There are 26 named tweaks. Key-level parity remains conservative at 96 done / 4 n/a / 99 todo, including partial native coverage; the full port is not complete.

## Goal

Port every YTAfterglow iOS tweak to Android YouTube **21.43.523**, exactly as it behaves on iOS: same settings, defaults, screens, translations and theme/glow look. Deliver it as the user's own standalone Morphe patch source, a `.mpp` bundle that Morphe Desktop or Manager loads next to Morphe's own list.
- Each Afterglow feature group is a separately named tweak in Morphe's list (names from `tools/tweaks.json`).
- An Afterglow settings page lives inside YouTube's settings and shows only rows for tweaks that are installed and implemented.
- Plumbing such as GmsCore/microG sign-in comes from Morphe's own "GmsCore support" patch, ticked in the same run.

## Current native action refresh checkpoint — 2026-11-14

Share and Save now refresh live under the video and in landscape fullscreen,
independently and together. The shared native reactive component bridge retains
the latest input/context, replays its original conversion, and calls the native
state update. Disposal clears retained inputs; a weak registry avoids retaining
closed players. The previous fullscreen view/presenter reset hook is removed.

Build 40 passes the complete live matrix, row scrolling, fullscreen return and
resumed playback, with no Afterglow errors and an empty crash buffer. A fresh
portrait fullscreen baseline does not offer Share at the tested width; its
available Save button hides and restores correctly. Earlier assertions requiring
Share there were test errors. See [`docs/player-behavior.md`](docs/player-behavior.md).

Build 41 removes diagnostic logging. Strict standalone/GmsCore/streaming runs
pass 26/27/28, all exit 0; its 214 production inputs match and ZIP integrity passes.
The bundle is `build-41/YTAfterglow-0.1.0-local.mpp` in the evidence root below.
Final build-41 emulator verification passes all 11 live cases and resumed
playback; standalone Player buttons and menus isolation passes 1/1, exit 0.
The final runtime log has no errors and the crash buffer is empty. Hide switches
are restored off, diagnostic properties are INFO and the app is stopped.
46 action-filter and 196 settings checks pass. Parity is
**96 done / 4 n/a / 99 todo**, with 26 named tweaks. Commit/push and deliver this checkpoint, then continue playback end time and
the remaining full port.

## Previous native overlay checkpoint — 2026-11-14

Build 31 adds title, background shading, fullscreen actions and channel watermark
switches to **Player overlay**. All nine overlay switches default off and reset
correctly. The Android 16 emulator verifies independent hides/restoration, combined
fullscreen/portrait behavior, watermark playback and retained control fades.

**Player buttons and menus** now handles fullscreen Share/Save independently with
live restoration. It resets the native cached presenter, restores its current
model and re-emits the native measured dimensions. Shared icon data caused the
old under-video filter to hide neighboring buttons; matching native accessibility
IDs fixes that. Fresh-playback under-video tests pass. Its cached bar still needs
the video reopened after a setting change, so Share/Save remain partial. Continue
that live-refresh boundary, then the remaining full port.

Strict build-31 runs pass 26/26 standalone, 27/27 with GmsCore and 28/28 with
GmsCore plus Spoof video streams, all exit 0. Both independently selected groups
also pass standalone strict isolation on build 30. 46 action-filter regression
checks and 196 settings checks pass. There are 214 recorded production inputs.
Candidate: `build-31/YTAfterglow-0.1.0-local.mpp` under the evidence root below;
the signed emulator APK is `overlay-test-31.apk`. Final build-31 checks confirm
live fullscreen hide/restore and independent under-video hides after reopening.
The final runtime log has no Afterglow errors and its crash buffer is empty;
defaults are restored and the app is stopped. Delivery provenance is recorded in
`delivery-31.json` under the evidence root. See [`docs/player-behavior.md`](docs/player-behavior.md).

## Previous fullscreen checkpoint — 2026-11-14

Build 27 adds separately selectable **Fullscreen**: automatic entry after the iOS
750ms delay, exit at playback completion, and retaining fullscreen through portrait
rotation. Native entry/exit and orientation policy are used. Session checks cancel
stale work; manual exit does not trigger reentry. All three settings default off.
Android 16 verifies independent entry/exit, immediate native autoplay, portrait
rotation with correctly letterboxed playback, restoration when disabled, and
preservation of the native Shorts player with all three switches on.

Strict validation passes 26/26 standalone, 27/27 with GmsCore and 28/28 with GmsCore
plus Spoof video streams, all exit 0. 192 settings and 15 session-race checks pass.
Candidate: `build-27/YTAfterglow-0.1.0-local.mpp` under the evidence root below;
210 source inputs recorded. The emulator has signed `fullscreen-test-27.apk`.
See [`docs/player-behavior.md`](docs/player-behavior.md). Continue the remaining
native player overlay and parity work; the full port remains active.

## Previous gesture checkpoint — 2026-11-14

Build 24 adds independent ordinary/two-finger double-tap switches and disables
continuous free zoom while preserving native pinch-to-fill. The switches reset
and restore native behavior; Android 16 verifies both gesture classifications,
**Zoomed to fill / Original**, and restored **1.8x** continuous zoom. One-finger
quick-scale was unavailable in the native baseline; its saved-flag restoration
is source-verified. SponsorBlock markers now follow native track fading and
still render in Shorts. See [`docs/player-behavior.md`](docs/player-behavior.md)
and [`docs/sponsorblock.md`](docs/sponsorblock.md).

Strict validation passes 25/25 standalone, 26/26 with GmsCore and 27/27 with
GmsCore plus Spoof video streams. Settings checks pass 175/175. Candidate:
`build-24/YTAfterglow-0.1.0-local.mpp` under the evidence root below. The emulator
has signed `gesture-test-24.apk`; no physical device was used. Fullscreen behavior
and the remaining parity list are the next work; the full port remains active.

## Previous SponsorBlock checkpoint — 2026-11-14

Build 20 adds the independently selectable **SponsorBlock** patch: all 21 iOS
preferences, nine independent skip/marker categories, a hash-prefix client,
active-player native seeking, a four-second Undo notice and timeline markers.
87 policy, 151 settings and 438 theme checks pass. Strict runs pass 25/25 standalone,
26/26 with GmsCore and 27/27 with GmsCore plus official **Spoof video streams**.
Android 16 verifies watch/fullscreen and Shorts markers, skip/undo, category/marker
independence, the Shorts-only switch and reset. See [`docs/sponsorblock.md`](docs/sponsorblock.md).

Shorts initially stalled because native Onesie media requests received HTTP 413.
Official **Spoof video streams** fixes playback in this emulator; select it alongside
GmsCore for subsequent runtime checks. `tools/verify_bundle.py --official-patch
"Spoof video streams"` selects it from `--gms-bundle`. The Afterglow bundle does not
include or automatically enable the official client. Build 20 used the
signed `sponsor-streaming-test-20.apk`, with Afterglow settings restored to defaults.

## Previous player checkpoint — 2026-11-14

The current candidate is `build-18/YTAfterglow-0.1.0-local.mpp` under the evidence
root below. **Player gestures** adds native tap-to-seek; **Disable ambient mode**
suppresses the cinematic renderer without changing YouTube's own saved setting.
Both default off and are independently selectable. **Player overlay** adds five
live hide switches for fullscreen, More, collapse, subtitles and previous/next.
Strict validation passes 24/24 standalone and 25/25 with GmsCore; 106 settings
checks pass. Actual emulator switches, the seek position change, ambient lighting
suppression, portrait/fullscreen control hides, playback fades and reset were verified.
See [`docs/player-behavior.md`](docs/player-behavior.md).

## Delivered theme checkpoint — 2026-11-14

The active checkout remains `/home/corey/projects/ytafterglow-morphe-patches`, `main`.
The current candidate is `build-15/YTAfterglow-0.1.0-local.mpp` under
`/mnt/c/Users/Corey/ytag-scratch/android/port-completion-20261113/`.

- Theme Studio: private drafts, 20 semantic roles, foundation/gradient/contrast helpers,
  save/restart, custom library, duplicate, rename, delete, JSON export and JSON/XML import.
- Added separately selectable Typography, Glow and Seek bar patches. 43 unchanged iOS font
  assets; Apple system-only families use Android counterparts. See `docs/font-assets.json`.
- Native feed gradients, navigation/icon tint, neutral text spans, TextView/layout/font-registry
  adapters, styled text, icon bloom, canvas glow, custom PNG scrubber and size/motion policy.
- Strict `--validate` passes with exit code 0: 21/21 standalone and 22/22 with official GmsCore.
  438 theme checks and 83 settings visibility checks pass, including independent patch gating.
- The signed candidate installed on the Android 16 phone emulator. Verified custom save and
  restart, exported/imported theme JSON, XML import, persistent library, discard-without-saving, rename,
  delete, native gradients, tab/player glow, selected font in feed/watch/comments/settings,
  custom PNG/size selection and scrubber rendering, portrait/fullscreen video playback and seeking.
- Typography/seek/color resets restore defaults and preserve the saved library. The final
  build fixes thumbnail mask errors found in build 12; the repeated fullscreen case has no
  YTAfterglow errors and an empty crash buffer.

Important fixes from runtime testing: keep resource background colors opaque for Android's
contrast calculation; clear only verified page/drawable backgrounds for gradients; apply fonts
at measurement and styled-span boundaries; draw the custom scrubber after track bloom.
See [`docs/theme-studio.md`](docs/theme-studio.md) and [`docs/port-progress.md`](docs/port-progress.md).

Remaining native theme work includes buffered/live-specific scrubber roles, broader server
control coverage and Shorts/live/promo-player verification. Exact iOS rasterization is not
claimed. Other port groups below remain unfinished. Keep them tracked; do not mistake this
validated theme delivery for all 199 keys being ported.

## Historical native theme checkpoint — 2026-11-13

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

## Historical takeover state

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

1. **Continue the port from the current checkpoint:** the Theme Studio editor/library/transfer and native gradient/font/glow adapters now exist. Remaining native role and player coverage is listed in `docs/theme-studio.md`; then continue the other feature groups in `tools/parity.json`. Do not ask the user to select the already authorized batch again.
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

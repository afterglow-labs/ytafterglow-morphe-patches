# Android port execution ledger

## Session Ledger Checkpoint — 2026-11-13

- Current user intent: finish the Android port, continuing autonomously through the remaining implementation and validation. Do not stop at another batch summary or ask the user to restart the work.
- Target: YouTube 21.43.523; standalone Afterglow patch bundle alongside official GmsCore support. iOS source is the behavior specification.
- Verified starting state: clean `main`, origin `afterglow-labs/ytafterglow-morphe-patches`, commit `9e10493a90caa0235b7ad93a2a4eb5119df59da1` pushed.
- Existing proof: native solid themes, 30 presets, light/dark foundation, restart and reset; 359 JVM checks; strict standalone and GmsCore application; Android 16 emulator.
- Outstanding scope: tracked by `tools/parity.json` (139 todo at start), including Theme Studio editor/library/transfer, native gradients, typography/glow, player/seek-bar controls, tabs, SponsorBlock, Control Center, downloads and other destinations.
- Next action: complete the semantic theme document, durable custom library and editor using the iOS file format; then connect native rendering and continue the parity list.
- Constraints: preserve defaults, gate each feature on its installed named tweak, fail open; use strict `--validate` patch testing. Put build/runtime evidence on C:. Commit and push directly to main. No physical device is required for emulator validation.

## Implementation checkpoint

- Added `ThemeDocument`, bounded JSON codec, semantic-record reconciliation and atomic saved-library operations. Theme engine: 400 checks pass. Existing settings visibility: 67 checks pass.
- Theme Studio now opens a draft editor with all 20 roles, contrast values, foundation, gradient, glow and typography; duplicate, rename, delete, import/export flows are wired. File import supports JSON and XML plist. Cancellation does not change active settings.
- Added native view attachment, gradient transparency/host background, semantic text-span colors and typography hooks. Copied the 43 existing bundled iOS font files into patch resources (16.7 MB). Android system counterparts are used for Apple system-only families; this is not exact Apple font fidelity.
- Build 02 compiled and passed all 20 selected patches with GmsCore (`combined-02`). Verified 37 text span constructors and 14 StaticLayout builder calls. Signed APK installed successfully on emulator-5600.
- Emulator UI proof so far: preset Duplicate & Edit; rename draft to QA Theme; background color changes to #172336 with preview/contrast updates. Save/restart/library and native gradient verification are in progress.
- Build 03 failed strict validation: Shorts resolves its color across two branches outside the initial search window. Fixed register-lifetime tracing; build 04 and build 05 each pass all 22 selected patches with GmsCore.
- Custom theme save/restart/persistence verified: QA Theme, #172336 background, custom identity retained in My Themes. JSON exported through SAF and parsed on the host with gradient, typography and glow fields intact.
- Gradient test exposed a launch crash in MediaRouteButton: Android contrast calculation rejects translucent theme backgrounds. Resource backgrounds now stay opaque (with compositing for translucent user colors); transparency is restricted to page views and Litho drawables. Build 05 launches with an empty crash log. Remaining opaque containers were identified from the actual UI tree and handled in build 07; native gradient visibility is still being checked.
- Build 06 adds verified native seek-bar geometry access, track glow, custom PNG scrubber, and 200ms transitions. Typed color/image pickers replace the placeholders. Build 07 adds page-container clearing and settings typography. Combined strict runs are in progress. Current installed emulator build is 05.
- 433 JVM theme/motion/glow checks and 67 visibility checks pass. Added a regression check for the opaque resource/transparent drawing boundary. New controls are tracked as partial while runtime coverage is checked; internal color/image keys now have an explicit implementation map validated by check_tweaks.
- Evidence root: `/mnt/c/Users/Corey/ytag-scratch/android/port-completion-20261113/`. AVD YTAG_Glow_Check is running on port 5600. The connected physical device has not been touched.
- All changes after 9e10493 are uncommitted. Continue runtime gradient/font/glow/player and file import checks, finish native role coverage, then update documentation, commit/push verified work and continue the remaining parity list. Do not report the full port finished.

## Session Ledger Checkpoint — 2026-11-14

- Latest user steering was conversational ("it made me laugh"); Android work continues under existing authorization. No permission or batch-selection question is pending.
- Build 09 passed all 22 selected patches with GmsCore. Installed and verified native feed gradient, imported Share Tech Mono on tabs/settings, native tab glow, theme import/restart, and video playback. Feed titles still used YouTube's own font registry; added a verified Laszn font-return adapter for styled text.
- Replaced banded concentric seek glow with cached radial/linear falloff and preserved native paint alpha. Native icon overlays now follow visibility, ancestor alpha, selection, and animated drawable changes.
- Build 11 passes strict --validate both standalone (21 patches) and with GmsCore (22). Both verifier processes exited 0. Its signed APK installed on emulator-5600; final runtime checks are active.
- 438 theme checks and 67 settings checks pass. Custom image picker saved a 64x32 PNG; the size picker is set to 100 percent for runtime inspection. Draft typography preview is the only source change after build 11.
- Font provenance verified against the current iOS spec checkout: 43 byte-identical files, 16,650,840 bytes; docs/font-assets.json records hashes and source commit.
- Next: verify registry fonts and smooth player glow, build the final preview fix, confirm cancel/rename/delete and reset, update current docs/parity evidence, commit/push and deliver the uniquely named .mpp. Full port still includes the remaining parity list; no claim of full completion.

## Final theme validation checkpoint

- Build 12 passes strict application: 21/21 standalone and 22/22 with GmsCore. 438 theme checks and 83 visibility checks pass.
- Runtime proof includes native fonts in feed/watch/comments/settings, full custom scrubber above track bloom, seeking and portrait/fullscreen controls. JSON and XML theme imports, export, draft discard, rename, delete, typography/seek reset and system color reset all passed. The normal YouTube settings entry works; the three saved QA themes survive reset.
- The crash buffer is empty, but the detailed YTAfterglow log exposed repeated nonfatal software-canvas errors from a fullscreen video thumbnail (YouTube's bitmap-backed Lvpn drawable). Do not publish build 12 as error-free. Glow/tint selection is now restricted to verified player icon resource IDs; bitmap artwork is skipped. Unsupported drawables are remembered weakly so a failed mask is not retried every frame, and failed allocations are recycled.
- Build 14 contains that correction (build 13 was intermediate). Next: strict validate build 14, install, repeat the fullscreen trigger and inspect the correct YTAfterglow log tag, then commit/push and deliver.
- After delivery, continue the unfinished player features and remaining parity list under the existing authorization. The complete Android port is still unfinished.

- Build 14 passed both strict runs (21 standalone, 22 with GmsCore), verified its signature and installed successfully. The fullscreen thumbnail trigger was repeated with QA Glow Mono active; glow still renders, the video thumbnail keeps its native pixels, runtime-fullscreen-14.log has no errors and crash-fullscreen-14.log is empty.
- Final theme candidate: build-14/YTAfterglow-0.1.0-local.mpp, 8,480,998 bytes, SHA-256 6a3d114e3281790836fafd3a4b819e708b89a39626d49a3f2257393364ab756a. Build input hashes are recorded in source-build-14.json under the evidence root.
- Ready to commit/push this validated theme checkpoint and create its delivery copy; the next active work is the remaining player/seek/ambient functionality.

- Build 14 eliminated the hardware-bitmap errors, but its screenshot exposed an overbroad bitmap exclusion that also removed glow from the verified fullscreen button. Build 15 keeps verified player icon IDs eligible, while excluding anonymous artwork. Both strict build-15 runs now pass (21/21 and 22/22); final signing/install/fullscreen checks are in progress.

- Build 15 is the validated theme delivery candidate: 8,481,002 bytes, SHA-256 4dea9bc06062fdb2985cbcc691d12db21c549444b1ca3836a7bad234a52fc3c8. Both strict runs exited 0, signature verification and installation passed, and all 187 recorded build inputs still match source. The fullscreen retest restores the verified bitmap icon's glow, leaves the thumbnail unchanged, and has no YTAfterglow errors or crashes. Screenshots/logs are fullscreen-controls-15.png, runtime-fullscreen-15.log and crash-fullscreen-15.log.

## Player behavior continuation

- Theme checkpoint committed and pushed to main as 0bdf544ce191713f639ede4636570c242b2753ce; remote main verified. Delivery: /mnt/c/Users/Corey/ytag-scratch/android/deliveries/YTAfterglow_Android_20261114T065902Z_yt21.43.523_0bdf544c.mpp. Matching .sha256 and .manifest.json include all 187 verified source input hashes.
- Emulator build 15 has been restored to default typography and colors. The saved QA library remains.
- Added next native boundaries: tap-to-seek delegates to verified Larua.S(I)/T(I) after Ljsb touch setup; disabling ambient mode empties only Lrir's cinematic renderer Optional and preserves loading state. Both settings remain todo/partial until strict and runtime verification.

- Build 16 failed a conservative seek-bar parent guard: the actual inheritance is Ljsb -> Ljrq -> Larua. Build 17 validates the independently fingerprinted bar instead of assuming a direct parent. Strict runs now pass 23/23 standalone and 24/24 with GmsCore, both exit 0.
- The signed build 17 installed and passed real UI toggles: tap-to-seek off leaves 0:01 unchanged, on seeks to about 0:15, and off again leaves 0:02 unchanged. Ambient lighting disappears with Afterglow enabled and returns with it disabled; the native Ambient preference stays on throughout the comparison. Both Afterglow switches and native ambient setting were restored off afterward.
- 97 settings checks pass, including isolated install/import/reset/Lite Mode gates. Runtime log has no YTAfterglow errors; crash buffer is empty. Parity is now 58 done / 4 n/a / 137 todo. No physical-device or live/DVR validation is claimed.
- Next: commit/push and deliver build 17; then implement the native player overlay visibility controls. The full port remains active.

## Player overlay continuation

- Build 17 committed/pushed as 8900a897437b8d1db18a721a3b277b91a2a0cf60; verified remote main. Delivered YTAfterglow_Android_20261114T071721Z_yt21.43.523_8900a897.mpp (8,493,985 bytes, SHA-256 a89af6c0edf6e909b661b272a62c671609f08703bb8a83505d229053e7d3c38a), with manifest and 190 input hashes checked against committed blobs.
- Added independently selectable Player overlay: captions, previous/next, fullscreen, collapse and overflow visibility. Verified native boundaries are Lolx.E (controls), Loex (captions), Lrud (collapse), Loft (fullscreen). Draw callbacks and native visibility remain intact; reversible offscreen positioning follows the requirement of the modern fullscreen control. Bindings follow attachment and preference revisions.
- Build 18 passes strict validation: 24/24 standalone and 25/25 with GmsCore. Settings checks pass 106/106. Candidate is signed/installing for portrait/fullscreen and live hide/restore tests. These five preferences remain partial until runtime checks finish; no physical device has been touched.
- Build 18 runtime is now verified: all five switches hide existing controls without restarting; restoring only Fullscreen brings back that control alone. Portrait/fullscreen transitions retain the other hides. Playback controls still fade out and return when paused. Reset restores all controls, including the native disabled Previous button. The five keys are done (63 done / 4 n/a / 132 todo). Runtime log has no errors and crash log is empty; Advanced mode and the five switches were restored off. Ready to commit/push and deliver, then continue the remaining port.

## SponsorBlock continuation

- Player overlay committed/pushed as 1701caacc81e35ced39caff89fe67ab360d34fae. Delivery: YTAfterglow_Android_20261114T073021Z_yt21.43.523_1701caac.mpp, 8,501,075 bytes, SHA-256 f552bd09f374fa437b3febf3e01ab0406bf44aa778852a2fae79733b21cadafa; 192 build inputs verified against the commit.
- Implemented the iOS SponsorBlock client, category defaults, generation-checked session, once-per-session skip/undo, overlapping marker layout, native timeline drawing and four-second Undo notice. Ljsb implements the native Shorts timeline interface Ljsq as well as the watch bar; the shared geometry hook covers both. Runtime verification is still required.
- 87 SponsorBlock policy checks, 151 settings checks and 438 theme checks pass. Build 19 compiles; strict standalone and combined GmsCore runs are active. All 21 SponsorBlock keys remain partial until runtime validation. The physical device remains untouched.

- Build 20 adds immediate active-video generation changes so A -> B -> A between ticks cannot retain the old session. Strict runs pass 25/25 standalone, 26/26 GmsCore and 27/27 GmsCore plus official Spoof video streams. Build 19 also passed the isolated SponsorBlock+GmsCore selection.
- Runtime watch/fullscreen skip, Undo, independent marker/category controls and reset passed. Native Shorts requests initially failed with HTTP 413; official Spoof video streams resolves that playback boundary. The unchanged build 20 then passed a real Shorts intro skip, blue marker and Undo to 0:00. Disabling only Shorts suppresses its client/marker/skip while watch sponsors continue skipping.
- All 21 SponsorBlock keys are done: 84 done / 4 n/a / 111 todo. 87 policy, 151 settings and 438 theme checks pass. Final logs have no YTAfterglow errors and an empty crash buffer; defaults restored. Next: commit/push and deliver this checkpoint, then continue the remaining player/gesture and other parity work. The full port remains active.


## Double-tap gesture continuation

- SponsorBlock committed/pushed as 33875d427c05f454a4cbf87cf5e593d5aa5d3499; remote main verified. Delivery: YTAfterglow_Android_20261114T081642Z_yt21.43.523_33875d42.mpp, 8,515,575 bytes, SHA-256 01e4e045554f2babea1c5da772a442b2df75da532b6c3ea4e39a850fcc37454a, with 202 committed build inputs verified.
- The emulator retains sponsor-streaming-test-20.apk (official GmsCore and Spoof video streams plus all Afterglow patches), all SponsorBlock defaults restored, app force-stopped. Continue using official streaming compatibility for runtime checks; physical device untouched.
- Next native boundary is Larxl.a(MotionEvent, View, boolean): constructs Larxo and dispatches gesture seeking and feedback. The boolean distinguishes chapter double taps. Added independent noDoubleTapToSeek and noTwoFingerSnapToChapter gates before dispatch, preserving native behavior when off. Both keys stay partial pending strict/runtime validation. Free zoom must retain pinch-to-fill per current iOS code; do not implement it by suppressing all pinch gestures.

- Build 21 passed strict standalone/GmsCore/streaming selections (25/26/27). The paused-video gesture matrix confirms independent one-finger and two-finger gates, both-on suppression and reset. Capture logs also contain a UIAutomator registration collision from an interrupted host test; the app did not crash. The test helper now serializes navigation and leaves every settings activity before returning to playback.
- Build 23 adds the verified native free-zoom capability (Losm.aU, five reads) and reversible quick-scale suppression while retaining pinch-to-fill. All three strict runs passed; 175 settings checks pass. Runtime free-zoom checks are in progress on gesture-test-23.apk.
- Baseline zoom testing exposed SponsorBlock markers persisting after the native bar hid. Build 24 adds animated track bounds (Ljsb.aa) and the native animated paint alpha (Ljsb.c.j) to the bridge, so markers follow native fading without depending on scrubber presence (important for Shorts). Strict/runtime fade and Shorts rechecks pending.

- Build 24 is verified: strict standalone/GmsCore/streaming runs passed 25/26/27, all exited 0. The Android 16 emulator preserves pinch-to-fill (Zoomed to fill / Original), and disabling the setting restores continuous 1.8x zoom. One-finger quick-scale was unavailable in the baseline; only its saved-flag restoration is source-verified. Both double-tap classification switches independently block their gesture; reset restores both.
- The marker correction passes fullscreen fade/return and thin Shorts timeline tests. Final runtime-gestures-24.log has no errors; crash-gestures-24.log is empty. Controls and Advanced mode are restored off. 175 settings, 438 theme and 87 SponsorBlock checks pass; 205 production build inputs match the candidate. Parity: 87 done / 4 n/a / 108 todo.
- Ready to commit/push and deliver build 24, then continue fullscreen entry/exit and the remaining port. No physical device was used; full-port completion is not claimed.

## Fullscreen continuation

- Gesture checkpoint committed/pushed to main as daa3c05040f491af17c628ba6b06ddbfcae4ece6; remote verified. Delivery: YTAfterglow_Android_20261114T085956Z_yt21.43.523_daa3c050.mpp, 8,530,429 bytes, SHA-256 b2f63e0c51eefa49cd7007213ed9cfbfaa4a8a32ffbc38ebaf2eb91cd4d68f44; 205 input hashes verified against the commit.
- Added native fullscreen bridge discovery and delayed once-per-playback entry/finish exit. 15 session-race checks and 190 settings checks pass. Portrait orientation remains hidden until implemented. Build 25 failed on a newer Morphe API import; build 26 adapts it to the local patcher 2.1 member API. Runtime and strict verification remain pending.
- Build 26 passes strict 26/27/28 selections, signs and installs. Actual UI: automatic entry off remains portrait; on enters fullscreen, and manual exit stays portrait through resume. Added a native portrait rotation guard for build 27 (Lkri.e reads Lkrj.h, maintainWatchState); it preserves an existing fullscreen player without forcing entry or changing Android orientation settings. Build 27 strict runs and remaining end/rotation checks are active.
- Build 27 completes all three fullscreen controls. Sensor rotation off/on/off verifies native exit versus retained portrait fullscreen; resumed decoded video is correctly letterboxed. All switches enabled leave Shorts in its native player. The three all-feature strict runs pass 26/27/28 and exit 0; settings 192/192 and session races 15/15 pass. The isolated three-patch run writes success and a valid APK, but its wrapper exits 143 after saving; a separate result/ZIP audit passes and records that distinction.
- Final controls reset is verified, acceleration restored, Advanced mode remains off, runtime-fullscreen-27.log has no errors and crash-fullscreen-27.log is empty. All 210 build inputs match. Parity: 90 done / 4 n/a / 105 todo, 26 named patches. Next: commit/push/deliver this checkpoint, then continue native overlay title/background/actions/watermark and the remaining full port.

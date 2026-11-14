# Native player behavior

The **Player gestures** and **Disable ambient mode** patches are independently
selectable. Both use the existing iOS preference keys and default to native
YouTube behavior. Importing a preference cannot activate an uninstalled patch.

## Tap to seek

`Player > Controls > Tap to seek` uses the native seek-bar touch coordinates and
calls the same update/seek methods as YouTube. On 21.43.523, the fingerprint resolves
`Ljsb.onTouchEvent`, with `Larua.S(I)` and `Larua.T(I)` reached through the intermediate
`Ljrq` superclass. The hook runs after the native touch setup; a separate helper
keeps the original method's register allocation unchanged.

## Double-tap gestures and free zoom

The same **Player gestures** patch also exposes the independent one-finger and
two-finger double-tap switches and **Disable free zoom gesture**. All default off.
The double-tap gate runs before native seek and feedback dispatch in `Larxl.a`;
its boolean argument identifies the native chapter gesture. Native chapter
availability remains controlled by YouTube.

Disabling free zoom preserves pinch-to-fill. Five reads of `Losm.aU` select the
player's existing fill/original path instead of continuous zoom; the underlying
native capability is retained so switching off restores it immediately. The
`Losu` player detector also saves and disables its one-finger quick-scale flag,
then restores the saved value on the next gesture after switching off.

Build 24 passes strict validation with all 25 Afterglow patches, 26 with GmsCore,
and 27 with GmsCore plus official Spoof video streams. Settings checks pass
175/175. The Android 16 emulator verifies:

- With only two-finger seeking disabled, two fingers leave 0:00 unchanged while
  one finger seeks to 0:10. With only ordinary seeking disabled, one finger leaves
  0:10 unchanged while two fingers seek to 0:22.
- Both switches block both gestures. Reset restores both. These are native
  gesture-classification tests; they do not establish a next-chapter destination.
- With free zoom disabled, pinch-out shows **Zoomed to fill**, and pinch-in shows
  **Original**. Switching the preference off restores continuous zoom to **1.8x**.
- One-finger zoom was unavailable in the native baseline. Its saved-flag
  restoration is verified in source, without claiming runtime activation.

Evidence in the directory below includes `gesture-*-21-{before,after}.xml`,
`freezoom-verified-fill-23-live.png`, `freezoom-verified-original-23-live.png` and
`freezoom-restored-decoded-continuous-23-live.png`. Build 24 has the same gesture
code as build 23 and additionally fixes SponsorBlock marker fading.

## Disable ambient mode

`Themes > Effects > Disable ambient mode` filters the native cinematic renderer's
Optional in `Lrir` while preserving its loading state. The old ambient feature
flags no longer exist in this target. When the Afterglow switch is off, the same
Optional is returned unchanged. YouTube's own Ambient mode preference is preserved.
The renderer gate applies when YouTube constructs the renderer state; reopening a
video guarantees that boundary is crossed. Lite Mode's existing policy forces
this gate on without overwriting the stored choice.

## Validation

Candidate `build-17/YTAfterglow-0.1.0-local.mpp` in
`/mnt/c/Users/Corey/ytag-scratch/android/port-completion-20261113/`:

- Strict `--validate`: 23/23 standalone; 24/24 with official GmsCore. Both exited 0.
- Settings validation: 97/97 checks, including independent installation, imported
  preferences, resets and Lite Mode behavior.
- Signed APK signature verified and installed on the Android 16 phone emulator.
- Actual UI switch: a tap 75 percent across the paused 19-second test video leaves
  the position unchanged when off and jumps from 0:01 to about 0:15 when enabled.
- With native Ambient mode enabled, the video visibly lights the status bar and
  watch-page background. Afterglow's disable switch removes that lighting while
  the native menu still shows Ambient mode on.
- Switching Afterglow's ambient override off restores the lighting. Turning
  tap-to-seek off again leaves a second 75 percent tap at 0:02. Both Afterglow
  switches and the native ambient preference were restored to their starting
  values. `runtime-player-17.log` has no errors; `crash-player-17.log` is empty.

Evidence includes `tap-enabled-after-17.png`, `ambient-native-paused-17.png`,
`ambient-forced-off-player-17.png` and `ambient-native-preference-preserved-17.xml`.
No physical-device or live/DVR proof is claimed. The full Android port remains in
progress; see `tools/parity.json` for each preference's status.

## Player overlay

The independently selectable **Player overlay** patch exposes nine switches in
`Player > Overlay` when Advanced mode is enabled:

- Hide video title
- Hide Fullscreen button
- Hide More button
- Hide Collapse button
- Hide Subtitles button
- Hide Previous and Next buttons
- Remove dark background
- Disable fullscreen actions
- Hide Watermarks

All default off. Bindings attach at the native controls (`Lolx.E`), captions
(`Loex`), collapse (`Lrud`) and fullscreen (`Loft`) controllers. Hiding moves the
control outside the viewport and removes its accessibility/focus target. Its
native visibility, enabled state, layout and draw callbacks remain unchanged;
the modern fullscreen button requires those callbacks for control fades.
Restoring the setting restores the properties Afterglow changed. Bindings detach
their listeners with the view, and cache preferences until their revision changes.

Build 18 passes strict `--validate` application: 24/24 standalone, 25/25 with
GmsCore, both exit 0. Settings checks pass 106/106. Its signed APK installed on the
Android 16 emulator. The default controls, all five live hide switches, individual
fullscreen-button restoration, orientation changes, playback fades and Reset to
Defaults were exercised. No restart was needed between hide and restore. The
restored Previous button remains natively disabled, and the other controls keep
their original enabled/clickable states.

Evidence in the same directory: `overlay-default-player-18.png`,
`overlay-live-hidden-18-dismissed.png`, `overlay-fullscreen-faded-18.png`,
`overlay-fullscreen-restored-18.png` and `overlay-portrait-restored-18.xml`.
The remaining overlay options stay hidden and tracked as unfinished. Inline
feed-player and physical-device behavior have not been runtime verified.
The five switches and Advanced mode were restored off after testing.
`runtime-overlay-18.log` has no errors and `crash-overlay-18.log` is empty.

## Fullscreen

The independently selectable **Fullscreen** patch uses the native `Lris.m()` and
`Lris.n()` entry/exit methods. A weak bridge is captured from `NextGenWatchLayout`.
Automatic entry waits 750 milliseconds after content activation, once per local
player/video session. It checks the active video again, requires a foreground
maximized watch player, and leaves minimized players, picture-in-picture, Shorts
and ads alone. Manually leaving fullscreen does not immediately reenter it.

Exit-on-finish acts after the native end-status update. Generation checks cancel
pending work when the player/video changes, including A -> B -> A transitions.
Portrait support filters the native `maintainWatchState` orientation guard while
an existing watch player is fullscreen; it does not force fullscreen entry or
rewrite Android's rotation preference.

Builds 26 and 27 pass strict 26/26 standalone, 27/27 with GmsCore and 28/28 with
GmsCore plus Spoof video streams. The session suite checks 15 cancellation and
once-per-playback cases; build 27 settings checks pass 192/192. Runtime build 26
confirms automatic entry off/on, manual exit without reentry and independent
exit-on-finish: with auto entry off, the same 19-second video remains fullscreen
at completion when exit is off, and returns to portrait when exit is on, including
immediate native autoplay into another video. Build 27 keeps fullscreen through
landscape-to-portrait rotation only when enabled; decoded video retains its aspect
ratio with letterboxing, and the native timeline and fullscreen controls remain
usable. Turning the setting off restores the original rotation behavior.

Evidence: `fullscreen-auto-{off,on}-26.*`, `fullscreen-manual-exit-stays-26.*`,
`fullscreen-default-ended-26-live.png`, `fullscreen-exit-on-ended-26-live.png`
and the timestamped rotation samples in `fullscreen-exit-on-observations-26.json`.
Cold launches select the intended fixture reliably; one reused VIEW intent left
the previous video open and was excluded from the results.

Portrait evidence: `portrait-native-return-27.xml`,
`portrait-enabled-return-27.xml`, `portrait-fullscreen-playing-27.png` and
`portrait-disabled-restored-27.xml`. The emulator acceleration was restored to its
original value. Physical-device, foldable/tablet and casting runtime remain
unverified.

Build 27 also preserves the native Shorts player with all three fullscreen
switches enabled (`fullscreen-excludes-shorts-27.*`). Controls reset restores all
three to off. The isolated Fullscreen + GmsCore + Spoof video streams run writes
a successful three-patch result and valid APK; its wrapper terminated with 143
after saving the output, so `fullscreen-only-27/result-audit.json` records the
separate result/ZIP audit. The three complete all-feature runs exited 0.

Final `runtime-fullscreen-27.log` contains no Afterglow errors;
`crash-fullscreen-27.log` is empty. `fullscreen-final-reset-27.xml` confirms all
visible Controls switches are off; Advanced mode stayed off throughout this batch.

## Additional native overlay surfaces

Title hiding binds the native `player_video_heading` parent in `Lrud`; collapse
and overflow are separate siblings. Background hiding binds the center, top and
bottom control scrims in `Lolx.F` plus `top_bar_background`, leaving ambient
lighting and the native button backgrounds intact. Fullscreen actions use the
`Loks` quick-actions container. These share the reversible visibility binding.

Watermarks have two implementations: the legacy `Locu` branding ImageView and
the modern `featured_channel_watermark_overlay` component. The latter follows
YouTube's component updates, including its per-second playback refresh. Build 28
verifies title, shading and actions independently, and the visible playing
watermark off/on/off. Controls fade normally; the runtime log has no errors and
the crash buffer is empty. Evidence: `overlay-title-{hidden,restored}-28.png`,
`overlay-shading-{hidden,restored}-28.png`, `overlay-actions-{hidden,restored}-28.xml`
and `overlay-watermark-{visible,hidden-playing,restored-playing}-28.png`.

Fullscreen Share and Save belong to **Player buttons and menus**, independently
of **Player overlay**. Both surfaces use native accessibility IDs and exclude
overflow descendants; their shared icon models cannot identify individual buttons.
Clip retains its dedicated component match.

The two action bars retain reactive Litho component state even when their host
views change. `WatchActionsPatch` discovers the `Lmuj` element conversion,
`Lvnb` component state and `Lvna` native update callback. It remembers the current
input and scoped context only for `video_action_bar.e` and `quick_actions.e`
components. Preference changes replay that conversion and notify its original
subscriber. The native state monitor and disposal checks guard the replay;
context transfers update the retained scope, disposal clears the inputs, and the
extension registry holds weak controller keys. The video stays open at its current
position. This replaces the previous fullscreen presenter reset/dimension bridge.

Build 40 verifies Share-only, Save-only, both hidden and live restoration under
the video and in landscape fullscreen, preserving Like/Dislike, Comments and
overflow where present. Scrolling the watch row away and back preserves the same
behavior. The final playback resumes. Runtime logs contain no Afterglow errors
and the crash buffer is empty. The exact diagnostic-free build 41 passes strict
26/27/28 standalone/GmsCore/streaming application, all exit 0. Its 214 production
inputs and bundle ZIP integrity are verified. Build 41 repeats all 11 live cases
and resumed decoded playback with no errors and an empty crash buffer. Isolated
Player buttons and menus validation also passes 1/1 with exit 0. Defaults are
restored, diagnostic logging is INFO and the app is stopped.

A cold-launch baseline establishes that portrait fullscreen does not offer Share
at the tested width even with both hide switches off. Requiring that unavailable
button to reappear was a test error, not a demonstrated cache bug. The available
Save button hides and restores live there, retaining Like/Dislike/Comments.
The automated under-video checks now require an actual `watch_list` host so a
native fullscreen gesture cannot silently change which surface is being tested.

Evidence under the directory above: `watch-actions-result-40.json`,
`watch-actions-*-40.*`, `runtime-watch-actions-40.log`,
`portrait-baseline-{before-settings,save-hidden,save-restored}-40.*` and
`runtime-portrait-baseline-40.log`. The 46 action-filter and 196 settings checks
also pass. Physical-device, tablet/foldable and casting behavior remain untested.

# Native player behavior

The **Player gestures** and **Disable ambient mode** patches are independently
selectable. Both use the existing iOS preference keys and default to native
YouTube behavior. Importing a preference cannot activate an uninstalled patch.

## Tap to seek

`Player > Controls > Tap to seek` uses the native seek-bar touch coordinates and
calls the same update/seek methods as YouTube. On 21.43.523, the fingerprint resolves
`Ljsb.onTouchEvent`, with `Larua.S(I)` and `Larua.T(I)` reached through the intermediate
`Ljrq` superclass. The hook runs after the native touch setup; a separate helper
keeps the original method's register allocation unchanged. Other player gestures
remain hidden until implemented.

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

The independently selectable **Player overlay** patch exposes five switches in
`Player > Overlay` when Advanced mode is enabled:

- Hide Fullscreen button
- Hide More button
- Hide Collapse button
- Hide Subtitles button
- Hide Previous and Next buttons

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
The rest of the overlay options remain hidden and tracked as unfinished. Inline
feed-player and physical-device behavior have not been runtime verified.
The five switches and Advanced mode were restored off after testing.
`runtime-overlay-18.log` has no errors and `crash-overlay-18.log` is empty.

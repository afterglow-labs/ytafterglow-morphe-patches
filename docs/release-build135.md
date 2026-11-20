# Afterglow build135 preview

[Release](https://github.com/afterglow-labs/ytafterglow-morphe-patches/releases/tag/v0.1.0-build.135) · [Add source](https://morphe.software/add-source?github=afterglow-labs/ytafterglow-morphe-patches&name=Afterglow)

Build135 fixes dragging tabs in **Afterglow settings → Interface → Tab bar →
Customize Tabs**, restores missing native tab labels, and aligns the icons with
labels both visible and hidden. It retains all 128 selectable patches, including
the 105 defaults, Afterglow's icon, downloader, Vault and Theme Studio, and the
integrated Morphe features. Patch an unmodified **YouTube 21.43.523** APK using
the refreshed Afterglow source.

## Changes

- Dragging previously removed the row receiving the gesture. Android immediately
  cancelled that gesture, cleared the drag state, and the move handler tried to
  add a null child. The touched row now stays attached while a separate placeholder
  moves. Only a completed drop saves the order; cancellation and detachment clean
  up without changing preferences.
- YouTube's icon-only and avatar-only tab layouts omit their label views and can
  receive an extra 16dp top padding. Afterglow now selects the corresponding native
  labelled layouts before the constructor classifies the padding. Native titles,
  icons, avatars and navigation commands are retained.
- Hiding labels now restores their prior visibility when disabled, including on
  existing views. Native layout performs the resulting icon alignment.

## Verification

- Strict Desktop 1.20 / patcher 2.1 application and APK assembly pass for the
  105 defaults, all 128 patches, and Tab bar with only GmsCore and stream support.
- An isolated Android touch test reproduces the original reported crash, then
  passes 91 assertions across 11 scenarios with the fix: reordering, transfers
  between shown/hidden sections, cancellation, limits, repeated drags,
  auto-scrolling and detachment. It uses actual Android touch dispatch.
- A second Android fixture uses YouTube 21.43.523's actual layouts and classes.
  Its 186 assertions reproduce the original missing labels and uneven positions,
  including with labels hidden. Fixed native icons and the avatar align with
  labels shown, hidden, restored, and in RTL layout.
- Settings checks pass 540/540; native-tab checks pass 1,898/1,898. The bundle
  exports 128 patches, 105 defaults, 436 acyclic dependency objects and six
  forwarded upstream options.
- Actual patched APK inspection passes 20 tab-layout DEX checks and 51 existing
  watch-action checks. The rewritten register controls both native inflation and
  the subsequent padding classification.
- The default APK upgrades the Android 16 emulator in place. Dragging changes
  the tab order and preserves it when the editor reopens; the original order is
  restored after the test. Eight active tabs, including the bonus row, align with
  labels shown, hidden and restored without restarting between toggles. The five
  existing Vault items remain visible. Playback advances from 6 to 13 seconds
  with an empty crash buffer. The installed APK hash matches the candidate.

Regression runners are `tools/check_tab_editor_android.py` and
`tools/check_tab_presentation_android.py`. Each accepts a new `--out` directory,
uses a disposable probe package on an emulator, and removes that package afterward.
The presentation test requires an installed YouTube 21.43.523 app; neither runner
changes that app's private preferences or Vault data.

The reported JS-player warning is still under investigation. Samsung logs did
not capture its text. The bundled player-script retrieval and both startup solver
challenges passed from the host; that does not establish Android sandbox behavior.
No JS error reporting was suppressed and no speculative stream changes were made.

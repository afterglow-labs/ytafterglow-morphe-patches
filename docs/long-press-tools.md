# Long-press tools

Owner: `long-press-tools` / **Long-press tools**, from `tools/tweaks.json`.
All five iOS preferences default to `false`. This release implements
`copyWithTimestamp`. The experimental Morphe title/description adaptation for
`copyVideoInfo` failed its build109 runtime checks and is excluded from the
release patch graph. Its source and regression tests remain for follow-up work.
`copyVideoInfo`, `postManager`, `commentManager` and `saveProfilePhoto` remain
pending and must stay hidden in Android settings. Full iOS parity is incomplete.

## Timestamped links

Despite its feature-group name, iOS `YTAfterglow.x:2358` implements an ordinary
Pause-button press, after the original `didPressPause:`. It copies
`https://www.youtube.com/watch?v=<video ID>&t=<whole elapsed seconds>s` and does
not add a toast. This Android implementation follows that interaction and URL
format. Android may show its own system clipboard feedback.

The stock YouTube 21.43.523 binding is:

- `Lokb.<init>` installs `Loka` with discriminator 2 as the native
  `player_control_play_pause_replay_button` click listener.
- `Loka.onClick` reads `Lokb.i:Larrs` / `Larrs.a:Larrr`.
  `Larrr.b` is PLAYING, and that branch calls `Larrt.a()` at code offset
  `0x028e`, immediately followed by return. The PAUSED branch uses `Larrt.b()`.
- `Larrt.a()` checks its native `c:Z` readiness field, then calls the native
  pause command `Lassj.c()`.
- The patch inserts the extension callback immediately after that single UI
  pause call. It does not hook the common pause command, playback-state updates,
  automatic pauses, the Play branch, or the Replay branch.

`LongPressTools.pauseClicked` checks the installed owner and current setting on
every call. It also checks native readiness, foreground watch ownership, ad and
Shorts state, a valid video ID and known native time. It reads the ID and time
from the existing current-director `VideoState` bridge, rather than keeping a
second playback cache. Missing inputs preserve the existing clipboard. Clipboard
service failures are logged through `Afterglow.error` and do not propagate into
the native click handler.

Integration: the named tweak depends on
`app.afterglow.patches.menus.longPressToolsHookPatch`, which depends on
`videoStatePatch` only. The named tweak must mark `long-press-tools` installed.
`copyWithTimestamp` is the only shipped key in this group. The experimental
`longPressTextHookPatch` must not be registered as a dependency until its native
interaction works and passes runtime validation.

## Unshipped title and description experiment

Build109 included this experiment during validation. With **Copy video details**
enabled and a cold watch page, holding the title opened YouTube's native
Description panel instead of copying; pasting still returned the previous
timestamp URL. Holding the expanded description produced native selection
handles and the Android **Copy / Share / Select all / Web search** toolbar,
visible in the screenshot but absent from the XML assertions. It did not show
the experimental Afterglow copy dialog; this is not evidence that native text
copying itself is broken, and that native selection must be preserved.
Evidence is in `bundle-109-qa/text-and-images` and
`bundle-109-qa/description-copy` under the build evidence directory
`/mnt/c/Users/Corey/ytag-scratch/android/port-completion-20261113`.

The native routing has a concrete gap: modern text uses `Lvuk`, an
`AppCompatTextView`, while the experiment attaches its long-click listener to a
`ComponentHost` or its ancestor. `Lvuk.onTouchEvent` calls the native superclass
and can call `performClick()` itself. `Lcci.S` does expose mounted `TextView`
contents to the host, but that alone does not establish gesture ownership.
The current fade-only title is also intentionally rejected by the conservative
text-identity policy. Existing runtime logs do not isolate which of matching,
native listener preservation or child dispatch rejected each attempt. No
production diagnostic hook or speculative listener replacement was added.

The remaining description of this experiment documents its intended behavior
and native mappings, not working release functionality.

The user authorized integrating matching Morphe implementations until the exact
Afterglow versions are ready. The adapted source is pinned to Morphe commit
`44031c561ea048c8f196877f30a49c13bfd6afb6`, [Copy text PR 3592](https://github.com/MorpheApp/morphe-patches/pull/3592),
specifically [CopyTextPatch.java](https://github.com/MorpheApp/morphe-patches/blob/44031c561ea048c8f196877f30a49c13bfd6afb6/extensions/youtube/src/main/java/app/morphe/extension/youtube/patches/CopyTextPatch.java)
and [TextComponentPatch.kt](https://github.com/MorpheApp/morphe-patches/blob/44031c561ea048c8f196877f30a49c13bfd6afb6/patches/src/main/kotlin/app/morphe/patches/shared/misc/textcomponent/TextComponentPatch.kt).

The intended interaction with **Copy video details** ON is:

- Long press the watch-page title to copy the displayed title.
- Long press a description text segment to open a dialog with selectable text.
  **Copy** copies that whole observed segment; Android's selection actions can
  copy a selected part. **Cancel** dismisses it.
- The same description-text recognition includes the native Shorts description
  element. This requires its own runtime check.

The native description is a list of text segments and attachments. This fallback
does not reconstruct a raw description or combine text across embedded channel
cards. It does not add the iOS Copy icon or its two-action header sheet. The
preference remains hidden; any future exposed subtitle must identify the
temporary behavior, and the iOS parity record must retain the incomplete
header/raw-description scope.

Verified stock 21.43.523 hook locations:

- `Lvuw.aC(Lhfs;)Lhfn;`: native creation and cached lookup converge at `0x00b5`.
  `v4` still holds `Lvuw.c:Lwuw`, and `v3` is the full text subsequently written
  to `Lhsl.y:CharSequence`. Register-lifetime checks guard the insertion.
- `Lvuz.L(Lhfs;Object;Lhhw;)V`: the cached text is observed before its native
  `SpannableString` constructor at `0x0069`. The fresh text is observed after the
  first `Lvuq.k` result at `0x00df`, while its conversion context is still live.
  The native renderer's creation flag, text and style spans remain unchanged.
- `ComponentHost.dispatchTouchEvent(MotionEvent)`: observation occurs before
  normal native dispatch. `ComponentHost.getTextContent().getTextItems()` supplies
  the text actually drawn by that touched host, through an added typed bridge.

Semantic recognition is restricted to Morphe's `video_metadata.e` title path,
`description_rich_text_list.e` and `cell_description_body.e`. Comments and posts
are not registered. Text caches are bounded; normalized matching retains source
line breaks inside copied text. Ambiguous truncated descriptions are rejected.
Prefix expansion requires an explicit ellipsis, with a separated optional native
label. A shared introduction or fade-only title does not establish text identity.

The original Morphe routing is adjusted for live settings and ownership. Native
text is observed while the owner is installed even when the setting is OFF, so
an already mounted view can work after enabling it. Touches and delayed long
presses both check the current preference. Each gesture captures its own text,
and rechecks the host before acting. A typed native listener bridge tracks every
`setOnLongClickListener` call, allowing cleanup to remove only Afterglow's own
listener while preserving a newer native listener on recycled hosts. Native
long-click handlers are not replaced, including retained handlers whose
`longClickable` flag is temporarily disabled by Litho.

The pinned upstream NOTICE is 994 bytes with SHA-256
`640950d68b69f45e1f05b58d796a0b11e818ee1cca69d56b103d6503d2c7047d`.
It is staged as investigation evidence at
`/home/corey/.cache/ytafterglow-android/team-106/long-press/Morphe-NOTICE-44031c561ea048c8f196877f30a49c13bfd6afb6.txt`.
Integration must preserve it in repository/package notices and user-visible
attribution alongside the existing notices.

## Remaining native routing

iOS `copyVideoInfo` does not use the long press described by its preference
subtitle. `YTAfterglow.x:4840` adds a copy icon to the Description panel header
identified by `video-description-ep-identifier`. That button presents exactly
**Copy title** and **Copy description**, using the player response's title and
raw short description. Both actions show the localized **Copied** feedback.

The stock Android native header class `Lakjr` retains its panel identifier in
`O:Ljava/lang/String;`, assigned by `A(String)`. Its `c(Context)` binds a native
title-header `LinearLayout`. The parent `Lakhj` passes the panel's identifier to
that setter; `Lakhh.M()` provides the owning panel identity from `Lbfgs`.
However, the reviewed build105 Description hierarchy uses a Litho header and
body. A `Lakjr` hook alone would not establish coverage of that current screen.

The Android player response `Lalfm` / `Lbhad.g:Lbhah` exposes video ID, title,
author and channel ID, but no verified short-description field. Its string
getters must not be relabeled as a description. `Lnhl.k:Lbfph` *is* explicitly
named `description` in its own `toString`, and `Lnrn` renders it through
`Lasze.n` into `Spanned` for a Description panel, but `Lnhl` identifies itself
as `MainDownloadedVideo`: this is offline metadata, not live-watch proof.
No offline-only or guessed raw-description hook is installed. The unshipped
Morphe experiment observes native semantic text as described above.

The iOS remaining actions provide the behavior contract for the next mapping:

- Profile image: long press 0.3 seconds, load the selected avatar at size 1024,
  then **Save profile picture** and **Copy profile picture**.
- Post: long press 0.3 seconds, **Copy post text**, optional selected-image
  save/copy, and save/copy the post as an image.
- Comment: long press 0.3 seconds, **Copy comment text**, **Save comment as
  image**, and **Copy comment as image**.

Their native Android model-to-mounted-view identity still needs verification.
No accessibility-text scraping or unsupported action button is introduced by
this release. The experimental text observer and touch hooks are excluded from
the release patch graph.

## Validation

`python3 tools/check_long_press_tools.py` runs the production URL policy and
clipboard hook against an observable clipboard and native readiness command.
It checks exact canonical URLs, subsecond truncation, wide times, invalid
identities, default OFF, absent owner, live OFF/reset, missing readiness,
foreground/ad/Shorts exclusions, navigation freshness and logged clipboard
failures. It also checks native title/description path recognition, title-marker
handling, collapsed text, whitespace/format normalization, multilingual text,
ambiguous matches, bounded eviction and excluded comment/post text. These are
host tests; they do not prove Android runtime behavior.

Build109 passed full 35-patch and independent six-patch strict application and
APK assembly. The Android 16 emulator recorded normal decoded playback advancing
and a native Pause at 9 seconds; pasting returned exactly
`https://www.youtube.com/watch?v=rfscVS0vtbw&t=9s`. Evidence is in
`bundle-109-qa/core-followup` and `bundle-109-qa/timestamp`. The next release
candidate must repeat integration checks after excluding the text hooks.

Further timestamp QA still needs to verify:

1. Full and independent Long-press-tools selections pass strict patch matching.
2. In normal portrait and fullscreen watch playback, Pause copies the current
   video ID and integer time; Play and Replay leave a sentinel clipboard intact.
3. Pause-on-overlay, app backgrounding and other automatic pause paths preserve
   the sentinel clipboard.
4. Default OFF, live OFF, cold persistence, page reset and full reset work.
5. A second video copies its own ID/time; unavailable playback and an ad do not
   copy a previous content ID. Shorts keep their native interaction.
6. Native pause/play still controls decoded playback. Physical devices and
   casting remain unverified until separately tested.
The unshipped text experiment additionally requires:

1. With `copyVideoInfo` ON, a long press copies the watch title and opens a
   selectable description segment. Copy, selected-text copy and Cancel preserve
   the original native links, expand/collapse and ordinary taps.
2. Text actions work after live ON, stop after live OFF/reset, persist cold, and
   reject recycled/detached text and conflicting native long presses. A new video
   must not copy the old title/description. Include Shorts-description testing.

Stock extraction evidence and investigation notes are under
`/home/corey/.cache/ytafterglow-android/team-106/long-press/`.

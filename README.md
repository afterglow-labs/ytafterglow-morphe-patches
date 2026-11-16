# YTAfterglow for Android

YTAfterglow patches for **YouTube 21.43.523**, distributed as a Morphe patch
source. The full iOS port is in progress; see [HANDOFF.md](HANDOFF.md) for current
coverage and validation limits.

## Add the source

In Morphe Manager, open **Sources → Add source**, paste this repository URL,
then select **Add**:

```text
https://github.com/afterglow-labs/ytafterglow-morphe-patches
```

[Open the add-source link](https://morphe.software/add-source?github=afterglow-labs/ytafterglow-morphe-patches&name=YTAfterglow)

For clients that ask for a JSON source URL, use:

```text
https://raw.githubusercontent.com/afterglow-labs/ytafterglow-morphe-patches/main/patches-bundle.json
```

The manifest points to the current verified `.mpp` on
[GitHub Releases](https://github.com/afterglow-labs/ytafterglow-morphe-patches/releases).
It updates when another verified build is published. A local `.mpp` can also be
imported from that release in Manager or Desktop.

## Current preview

Build105 provides 29 independently selectable feature groups. Its latest addition
is **Hide up-next panel**, retaining video information and comments. Validation
includes 459 settings checks, strict patch application and Android 16 emulator
checks. Physical-device testing remains. Full parity is not yet complete.

For build105's tested non-root setup, also select **GmsCore support** and
**Spoof video streams** from the official Morphe source. Integration of matching
Morphe implementations into this source is in progress; build105 does not yet
include those official patches. The verified toolchain is patcher 2.1.0 and
Morphe Desktop 1.20.0.

Upstream-derived code retains its attribution and license notices. See
[NOTICE](NOTICE) and [LICENSE](LICENSE).

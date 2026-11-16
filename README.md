# Afterglow for Android

Afterglow patches for **YouTube 21.43.523**, distributed as a Morphe patch
source. The full iOS port is in progress; see [HANDOFF.md](HANDOFF.md) for current
coverage and validation limits.

## Add the source

In Morphe Manager, open **Sources → Add source**, paste this repository URL,
then select **Add**:

```text
https://github.com/afterglow-labs/ytafterglow-morphe-patches
```

[Open the add-source link](https://morphe.software/add-source?github=afterglow-labs/ytafterglow-morphe-patches&name=Afterglow)

For clients that ask for a JSON source URL, use:

```text
https://raw.githubusercontent.com/afterglow-labs/ytafterglow-morphe-patches/main/patches-bundle.json
```

The manifest points to the current verified `.mpp` on
[GitHub Releases](https://github.com/afterglow-labs/ytafterglow-morphe-patches/releases).
It updates when another verified build is published. A local `.mpp` can also be
imported from that release in Manager or Desktop.

## Current preview

Build110 provides **35 independently selectable patches in one bundle**, including
**GmsCore support** and **Spoof video streams**, selected by default. Use an
unpatched **YouTube 21.43.523** APK and select the patches from the Afterglow source.
Use the default patch selection for the tested playback configuration.
Use this bundle's compatibility patches only; selecting another source's copies
applies overlapping hooks. A compatible GmsCore companion app is still required
for the non-root setup.

New controls include mini-player playback for children's videos, the old playlist
bar, timestamped links from Pause, Android's share sheet and alternate image hosts.
These settings start OFF. The playlist-bar switch is under advanced Player →
Playback settings and requires a restart. Title/description copying remains
pending and hidden; the full iOS port is still in progress.

Validation includes 518 settings checks, strict full and independent APK assembly
with Morphe Desktop **1.20.0 / patcher 2.1.0**, successful full patching in
**Morphe Manager 2.6.0**, and Android 16 emulator checks.
See [the release checkpoint](docs/release-build110.md) for tested behavior and limits.
Physical-device testing and account login remain unverified.

Upstream-derived code retains its attribution and license notices, also available
in **Afterglow settings → About → Credits**. See [NOTICE](NOTICE), [LICENSE](LICENSE)
and [the pinned compatibility sources](docs/morphe-compatibility.md).

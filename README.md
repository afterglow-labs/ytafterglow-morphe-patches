# Afterglow for Android

Afterglow patches for **YouTube 21.43.523**, distributed as a Morphe patch
source. Existing Morphe features are integrated alongside Afterglow's own
features. See [HANDOFF.md](HANDOFF.md) for current coverage and validation limits.

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

Build118 provides **122 selectable patches**, with **99 selected
by default**. It includes Morphe's **Return YouTube Dislike**, **DeArrow**,
**Downloads**, **Copy text**, **GmsCore support** and **Spoof video streams**.
Use an unpatched **YouTube 21.43.523** APK and select the Afterglow source.
Use the default patch selection for the tested playback configuration.
Use this bundle's compatibility patches only; selecting another source's copies
applies overlapping hooks. A compatible GmsCore companion app is still required
for the non-root setup.

Open **Afterglow settings → Additional features** for the imported Morphe
settings. Dislikes and title/comment copying are enabled by default. DeArrow's
title scopes are available there and start off. Downloads retains Morphe's
in-app and external-downloader choices. Original implementations and their
runtime defaults are preserved; selecting a patch does not turn on every setting.

The import adds 87 feature patches from the pinned Morphe **2.0.0** source.
Features already covered by Afterglow keep their existing owner; partial overlaps
are optional. Custom development focuses on features Morphe does not provide.
See the [complete feature mapping](docs/morphe-catalog.md) for all inclusions,
overlaps and the two excluded branding patches.

Validation includes 518 settings checks, strict patching and APK assembly for
99/default, 8/independent and 122/all selections, successful patching and signing
in Morphe Manager 2.6, and Android 16 emulator checks. See [the release checkpoint](docs/release-build118.md)
for exact selections and limits. Physical-device testing and account login remain unverified.

Upstream-derived code retains its attribution and license notices, also available
in **Afterglow settings → About → Credits**. See [NOTICE](NOTICE), [LICENSE](LICENSE)
and [the pinned imported sources](vendor/morphe/README.md).

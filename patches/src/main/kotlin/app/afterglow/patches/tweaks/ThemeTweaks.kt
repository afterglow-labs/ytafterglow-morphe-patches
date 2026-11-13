package app.afterglow.patches.tweaks

import app.afterglow.patches.shared.Constants.COMPATIBILITY_YOUTUBE
import app.afterglow.patches.shared.markTweakInstalled
import app.afterglow.patches.theme.nativeThemePatch
import app.morphe.patcher.patch.bytecodePatch

@Suppress("unused")
val themeStudioTweakPatch = bytecodePatch(
    name = "Theme Studio",
    description = "Adds Afterglow's 30 theme presets and native YouTube surface and text colors on Android 14+. Restart to apply. The custom editor and glow renderer are not yet included.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_YOUTUBE)
    dependsOn(nativeThemePatch)
    execute { markTweakInstalled("theme-studio") }
}

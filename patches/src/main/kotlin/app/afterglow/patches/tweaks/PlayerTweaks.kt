package app.afterglow.patches.tweaks

import app.afterglow.patches.playback.ambientModeHookPatch
import app.afterglow.patches.playback.tapToSeekHookPatch
import app.afterglow.patches.settings.afterglowSettingsPatch
import app.afterglow.patches.shared.Constants.COMPATIBILITY_YOUTUBE
import app.afterglow.patches.shared.markTweakInstalled
import app.morphe.patcher.patch.bytecodePatch

@Suppress("unused")
val ambientModeTweakPatch = bytecodePatch(
    name = "Disable ambient mode",
    description = "Lets you disable the video's ambient lighting while preserving YouTube's own behavior when off.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_YOUTUBE)
    dependsOn(afterglowSettingsPatch, ambientModeHookPatch)
    execute { markTweakInstalled("ambient-mode") }
}

@Suppress("unused")
val playerGesturesTweakPatch = bytecodePatch(
    name = "Player gestures",
    description = "Lets you seek by tapping the progress bar using YouTube's native seek handlers.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_YOUTUBE)
    dependsOn(afterglowSettingsPatch, tapToSeekHookPatch)
    execute { markTweakInstalled("player-gestures") }
}

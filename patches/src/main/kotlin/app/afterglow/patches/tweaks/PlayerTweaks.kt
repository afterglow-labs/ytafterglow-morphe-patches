package app.afterglow.patches.tweaks

import app.afterglow.patches.playback.ambientModeHookPatch
import app.afterglow.patches.playback.tapToSeekHookPatch
import app.afterglow.patches.playback.playerOverlayHookPatch
import app.afterglow.patches.playback.doubleTapGestureHookPatch
import app.afterglow.patches.playback.freeZoomHookPatch
import app.afterglow.patches.playback.fullscreenHookPatch
import app.afterglow.patches.playback.portraitFullscreenHookPatch
import app.afterglow.patches.litho.afterglowLithoFilterPatch
import app.afterglow.patches.settings.afterglowSettingsPatch
import app.afterglow.patches.shared.Constants.COMPATIBILITY_YOUTUBE
import app.afterglow.patches.shared.markTweakInstalled
import app.morphe.patcher.patch.bytecodePatch

@Suppress("unused")
val fullscreenTweakPatch = bytecodePatch(
    name = "Fullscreen",
    description = "Controls automatic fullscreen, exit on finish, and fullscreen playback in portrait orientation.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_YOUTUBE)
    dependsOn(afterglowSettingsPatch, fullscreenHookPatch, portraitFullscreenHookPatch)
    execute { markTweakInstalled("fullscreen") }
}

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
    description = "Controls tap-to-seek, independent double-tap gestures, and free zoom while keeping pinch-to-fill.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_YOUTUBE)
    dependsOn(afterglowSettingsPatch, tapToSeekHookPatch, doubleTapGestureHookPatch, freeZoomHookPatch)
    execute { markTweakInstalled("player-gestures") }
}

@Suppress("unused")
val playerOverlayTweakPatch = bytecodePatch(
    name = "Player overlay",
    description = "Lets you hide native player controls, title, background shading, fullscreen actions and channel watermarks.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_YOUTUBE)
    dependsOn(afterglowSettingsPatch, playerOverlayHookPatch, afterglowLithoFilterPatch)
    execute { markTweakInstalled("player-overlay") }
}

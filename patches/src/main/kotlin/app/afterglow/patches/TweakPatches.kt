package app.afterglow.patches

import app.afterglow.patches.litho.afterglowLithoFilterPatch
import app.afterglow.patches.menus.menuRowsHookPatch
import app.afterglow.patches.settings.afterglowSettingsPatch
import app.afterglow.patches.shared.Constants.COMPATIBILITY_YOUTUBE
import app.afterglow.patches.shared.afterglowExtensionPatch
import app.afterglow.patches.shared.markTweakInstalled
import app.morphe.patcher.patch.bytecodePatch

/*
 * The Afterglow tweaks as Morphe shows them: one named patch per tweak in
 * tools/tweaks.json, each with the name given there. A tweak's patch marks its
 * id in InstalledTweaks; the extension turns on only the features of installed
 * tweaks, and each feature still follows its YTAfterglow setting at runtime, as
 * on iOS. The settings page, the extension and the Litho filter are unnamed base
 * patches that every tweak depends on. tools/check_tweaks.py checks this file
 * against tools/tweaks.json.
 */

@Suppress("unused")
val shortsLayoutTweakPatch = bytecodePatch(
    name = "Shorts layout and buttons",
    description = "Hides the Shorts player buttons and overlay parts you turn off in YTAfterglow settings.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_YOUTUBE)
    dependsOn(afterglowExtensionPatch, afterglowSettingsPatch, afterglowLithoFilterPatch)
    execute { markTweakInstalled("shorts-layout") }
}

@Suppress("unused")
val shortsTweakPatch = bytecodePatch(
    name = "Shorts",
    description = "Lets you hide Shorts shelves and cards in Home, Subscriptions, search and related videos.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_YOUTUBE)
    dependsOn(afterglowExtensionPatch, afterglowSettingsPatch, afterglowLithoFilterPatch, app.afterglow.patches.tweaks.navigation.shortsHooksPatch)
    execute { markTweakInstalled("shorts") }
}

@Suppress("unused")
val playerButtonsMenusTweakPatch = bytecodePatch(
    name = "Player buttons and menus",
    description = "Lets you hide the Download, Share, Save, Remix and Clip buttons under the video, and the Download and Share menu rows.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_YOUTUBE)
    dependsOn(afterglowExtensionPatch, afterglowSettingsPatch, afterglowLithoFilterPatch, menuRowsHookPatch,
        app.afterglow.patches.playback.quickActionsHookPatch)
    execute { markTweakInstalled("player-buttons-menus") }
}

@Suppress("unused")
val watchPageTweakPatch = bytecodePatch(
    name = "Watch page",
    description = "Lets you hide the Continue watching card, the \"Are you still watching?\" pause, end-screen cards and end-screen related videos.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_YOUTUBE)
    dependsOn(afterglowExtensionPatch, afterglowSettingsPatch, afterglowLithoFilterPatch, app.afterglow.patches.playback.watchPagePlaybackHookPatch)
    execute { markTweakInstalled("watch-page") }
}

@Suppress("unused")
val commentsHeaderTweakPatch = bytecodePatch(
    name = "Comments header",
    description = "Lets you hide the sort chips at the top of the comments.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_YOUTUBE)
    dependsOn(afterglowExtensionPatch, afterglowSettingsPatch, afterglowLithoFilterPatch)
    execute { markTweakInstalled("comments-header") }
}

@Suppress("unused")
val removeAdsTweakPatch = bytecodePatch(
    name = "Remove ads",
    description = "Removes video ads and the ads and paid promotion labels in feeds and on the watch page.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_YOUTUBE)
    dependsOn(afterglowExtensionPatch, afterglowSettingsPatch, afterglowLithoFilterPatch, app.afterglow.patches.playback.videoAdsHookPatch)
    execute { markTweakInstalled("remove-ads") }
}

@Suppress("unused")
val navigationBarTweakPatch = bytecodePatch(
    name = "Navigation bar",
    description = "Lets you hide the Cast, Notifications, Search and voice search buttons and the filter chips, hide or swap the YouTube logo, and keep the top bar pinned.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_YOUTUBE)
    dependsOn(afterglowExtensionPatch, afterglowSettingsPatch, afterglowLithoFilterPatch, app.afterglow.patches.tweaks.navigation.navigationBarHooksPatch)
    execute { markTweakInstalled("navigation-bar") }
}

@Suppress("unused")
val feedMenusTweakPatch = bytecodePatch(
    name = "Feed menus",
    description = "Lets you remove rows such as Play next, Not interested and Report from video menus.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_YOUTUBE)
    dependsOn(afterglowExtensionPatch, afterglowSettingsPatch, menuRowsHookPatch)
    execute { markTweakInstalled("feed-menus") }
}

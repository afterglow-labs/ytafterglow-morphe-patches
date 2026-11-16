/*
 * YTAfterglow for Android.
 *
 * Hooks adapted from Morphe patches v2.2.0 (https://github.com/MorpheApp/morphe-patches, GPLv3),
 * forked from ReVanced patches (https://github.com/ReVanced/revanced-patches, GPLv3): the Change start page patch.
 * Modified for YTAfterglow: rewritten for patcher 2.1.0 and YouTube 21.43.523 and driven by the
 * iOS settings. Not an official Morphe or ReVanced file.
 *
 * Portions of this software are provided "AS IS" by the Morphe software project.
 * Any express or implied warranties, including the implied warranties of
 * merchantability and fitness for a particular purpose, are disclaimed.
 */

package app.afterglow.patches.tweaks.navigation

import app.afterglow.patches.shared.Constants.COMPATIBILITY_YOUTUBE
import app.afterglow.patches.shared.Constants.EXTENSION_PACKAGE
import app.afterglow.patches.shared.afterglowExtensionPatch
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

internal const val STARTUP_HOOKS = "$EXTENSION_PACKAGE/navigation/StartupHooks;"

/**
 * Where YouTube opens (iOS startupTab, and shortsOnlyMode's Shorts start): the default page's
 * browse id, the launch intent's action for Shorts, and the guide request's milestone. Used by
 * the named "Startup tab and animation" patch and by the Shorts hooks.
 *
 * startupAnimation is not ported: on 21.43.523 the animation is chosen inside
 * MainActivity.onCreate (flag 1074339245 picks the Lottie style, `Lrtt` plays it) and its
 * show/skip decision is spread over several flags; it needs a device to check.
 */
internal val startupHooksPatch = bytecodePatch(
    description = "Hooks YouTube's start page for the Afterglow startup settings.",
) {
    compatibleWith(COMPATIBILITY_YOUTUBE)
    dependsOn(afterglowExtensionPatch)

    execute {
        StartupBrowseIdFingerprint.let {
            val index = it.instructionMatches.first().index
            val register = it.method.instruction<OneRegisterInstruction>(index).registerA
            it.method.addInstructions(
                index + 1,
                """
                    ${invokeStatic(register, method = "$STARTUP_HOOKS->browseId(Ljava/lang/String;)Ljava/lang/String;")}
                    move-result-object v$register
                """,
            )
        }

        LaunchIntentFingerprint.method.addInstructions(
            0,
            "invoke-static/range { p1 .. p1 }, $STARTUP_HOOKS->launchIntent(Landroid/content/Intent;)V",
        )

        GuideRequestFingerprint.let {
            it.method.overrideFlagRead(it.instructionMatches.first().index, "$STARTUP_HOOKS->guideMilestone(I)I")
        }
    }
}

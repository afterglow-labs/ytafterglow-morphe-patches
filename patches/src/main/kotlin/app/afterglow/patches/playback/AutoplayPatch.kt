/*
 * YTAfterglow for Android.
 *
 * Adapted from the autoplay button part of the "Hide player overlay buttons" patch of
 * Morphe patches v2.2.0 (https://github.com/MorpheApp/morphe-patches, GPLv3), which was
 * forked from ReVanced patches (https://github.com/ReVanced/revanced-patches, GPLv3).
 * Modified for YTAfterglow: rewritten for patcher 2.1.0, gated by the iOS setting
 * autoplayMode. Not an official Morphe or ReVanced file.
 *
 * Portions of this software are provided "AS IS" by the Morphe software project.
 * Any express or implied warranties, including the implied warranties of
 * merchantability and fitness for a particular purpose, are disclaimed.
 */

package app.afterglow.patches.playback

import app.afterglow.patches.shared.Constants.COMPATIBILITY_YOUTUBE
import app.afterglow.patches.shared.afterglowExtensionPatch
import app.afterglow.patches.video.debugMatch
import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.literal
import app.morphe.patcher.methodCall
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.resource.ResourceType
import app.morphe.patcher.resourceLiteral
import app.morphe.patcher.util.smali.ExternalLabel
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

private const val AUTOPLAY_CLASS = "$EXTENSION_PLAYBACK/Autoplay;"

/** Builds the player controls layout, including the Autoplay switch (R.id.autonav_toggle). */
internal object PlayerControlsLayoutConstructorFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "V",
    filters = listOf(
        literal(159962),
        resourceLiteral(ResourceType.ID, "player_control_previous_button_touch_area"),
        resourceLiteral(ResourceType.ID, "player_control_next_button_touch_area"),
        methodCall(parameters = listOf("Landroid/view/View;", "I")),
    ),
)

/**
 * The "Off + hidden" option (2) of the iOS setting `autoplayMode` (tweak "autoplay"): iOS hides
 * the Autoplay switch of the player overlay. Here the code that inflates the switch from its
 * view stub is skipped when the player controls are built.
 */
internal val autoplayHookPatch = bytecodePatch(
    description = "Hides the player's Autoplay switch when YTAfterglow is set to.",
) {
    compatibleWith(COMPATIBILITY_YOUTUBE)
    dependsOn(afterglowExtensionPatch)

    execute {
        val layout = PlayerControlsLayoutConstructorFingerprint.method
        debugMatch("playerControlsLayout", layout)
        val toggleIndex = Fingerprint(filters = listOf(resourceLiteral(ResourceType.ID, "autonav_toggle")))
            .match(layout).instructionMatches.first().index
        val instructions = layout.implementation!!.instructions
        // The switch is inflated from its ViewStub by the first call taking (ViewStub, x).
        val inflateIndex = (toggleIndex until instructions.size).firstOrNull { index ->
            val instruction = instructions[index]
            val parameters = ((instruction as? ReferenceInstruction)?.reference as? MethodReference)?.parameterTypes
            instruction.opcode == Opcode.INVOKE_VIRTUAL && parameters?.size == 2 &&
                parameters.first().toString() == "Landroid/view/ViewStub;"
        } ?: throw PatchException("No Autoplay switch inflation in $layout")
        val register = layout.getInstruction<OneRegisterInstruction>(toggleIndex).registerA
        if (register > 15) throw PatchException("Autoplay switch id register v$register is too high")

        layout.addInstructionsWithLabels(
            toggleIndex,
            """
                invoke-static { }, $AUTOPLAY_CLASS->hideAutoplaySwitch()Z
                move-result v$register
                if-nez v$register, :afterglow_hidden
            """,
            ExternalLabel("afterglow_hidden", layout.getInstruction(inflateIndex + 1)),
        )
    }
}

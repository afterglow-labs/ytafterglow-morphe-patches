/* Native fingerprints adapted from Morphe patches v2.2.0 (GPLv3),
 * https://github.com/MorpheApp/morphe-patches. See NOTICE.
 * Rewritten for patcher 2.1 and Afterglow's independently installed, live settings.
 * Portions provided "AS IS" by the Morphe software project without express or implied
 * warranties, including merchantability and fitness for a particular purpose.
 */
package app.afterglow.patches.playback

import app.afterglow.patches.shared.Constants.EXTENSION_PACKAGE
import app.afterglow.patches.shared.afterglowExtensionPatch
import app.afterglow.patches.shared.invokeRegisters
import app.morphe.patcher.Fingerprint
import app.morphe.patcher.InstructionLocation.MatchAfterWithin
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.literal
import app.morphe.patcher.methodCall
import app.morphe.patcher.opcode
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.resource.ResourceType
import app.morphe.patcher.resourceLiteral
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

private const val OVERLAY = "$EXTENSION_PACKAGE/playback/PlayerOverlay;"

internal val playerOverlayHookPatch = bytecodePatch {
    dependsOn(afterglowExtensionPatch)
    execute {
        // Resolve a fresh match: the independently selected Autoplay patch also edits this method.
        val controls = Fingerprint(accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL), returnType = "V",
            filters = listOf(
                literal(159962),
                resourceLiteral(ResourceType.ID, "player_control_previous_button_touch_area"),
                resourceLiteral(ResourceType.ID, "player_control_next_button_touch_area"),
                methodCall(parameters = listOf("Landroid/view/View;", "I"))
            ))
        val index = controls.instructionMatches.last().index
        val root = controls.method.implementation!!.instructions[index].invokeRegisters.first()
        controls.method.addInstructions(index,
            "invoke-static/range { v$root .. v$root }, $OVERLAY->controls(Landroid/view/View;)V")
        println("Afterglow overlay controls: ${controls.method.definingClass}->${controls.method.name}")

        val captions = Fingerprint(accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
            returnType = "V", parameters = listOf("L"), filters = listOf(
                fieldAccess(opcode = Opcode.IGET_OBJECT, type = "Lcom/google/android/libraries/youtube/common/ui/TouchImageView;"),
                resourceLiteral(ResourceType.STRING, "accessibility_captions_unavailable"),
                resourceLiteral(ResourceType.STRING, "accessibility_captions_button_name")
            ))
        val captionRead = captions.instructionMatches.first().index
        val captionRegister = (captions.method.implementation!!.instructions[captionRead] as OneRegisterInstruction).registerA
        captions.method.addInstructions(captionRead + 1,
            "invoke-static/range { v$captionRegister .. v$captionRegister }, $OVERLAY->captions(Landroid/widget/ImageView;)V")

        val collapse = Fingerprint(returnType = "V", filters = listOf(
            resourceLiteral(ResourceType.ID, "player_collapse_button"),
            opcode(Opcode.CHECK_CAST),
            resourceLiteral(ResourceType.ID, "title_anchor"),
            opcode(Opcode.MOVE_RESULT_OBJECT)
        ))
        val collapseCast = collapse.instructionMatches[1].index
        val collapseRegister = (collapse.method.implementation!!.instructions[collapseCast] as OneRegisterInstruction).registerA
        collapse.method.addInstructions(collapseCast + 1,
            "invoke-static/range { v$collapseRegister .. v$collapseRegister }, $OVERLAY->collapse(Landroid/widget/ImageView;)V")

        val fullscreen = Fingerprint(accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
            parameters = listOf("Landroid/view/View;"), returnType = "V", filters = listOf(
                resourceLiteral(ResourceType.ID, "fullscreen_button"),
                opcode(Opcode.CHECK_CAST, MatchAfterWithin(10))
            ))
        val fullscreenCast = fullscreen.instructionMatches[1].index
        val fullscreenRegister = (fullscreen.method.implementation!!.instructions[fullscreenCast] as OneRegisterInstruction).registerA
        fullscreen.method.addInstructions(fullscreenCast + 1,
            "invoke-static/range { v$fullscreenRegister .. v$fullscreenRegister }, $OVERLAY->fullscreen(Landroid/widget/ImageView;)V")
        println("Afterglow overlay controllers: captions=${captions.method.definingClass}, collapse=${collapse.method.definingClass}, fullscreen=${fullscreen.method.definingClass}")
    }
}

/* Native quick-actions discovery/reset adapted from Morphe patches v2.2.0 (GPLv3),
 * https://github.com/MorpheApp/morphe-patches, interaction/nativeplaylistqueue.
 * See NOTICE. Afterglow retains the original model and only refreshes its own hide settings.
 * Portions provided "AS IS" without express or implied warranties, including
 * merchantability and fitness for a particular purpose.
 */
package app.afterglow.patches.playback

import app.afterglow.patches.shared.Constants.EXTENSION_PACKAGE
import app.afterglow.patches.shared.addMethod
import app.afterglow.patches.shared.addFieldGetter
import app.afterglow.patches.shared.afterglowExtensionPatch
import app.afterglow.patches.shared.smali
import app.morphe.patcher.Fingerprint
import app.morphe.patcher.InstructionLocation.MatchAfterImmediately
import app.morphe.patcher.InstructionLocation.MatchAfterWithin
import app.morphe.patcher.checkCast
import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.methodCall
import app.morphe.patcher.opcode
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.resource.ResourceType
import app.morphe.patcher.resourceLiteral
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction

internal val quickActionsHookPatch = bytecodePatch {
    dependsOn(afterglowExtensionPatch)
    execute {
        val binding = Fingerprint(returnType = "V", parameters = listOf("Landroid/view/View;"), filters = listOf(
            resourceLiteral(ResourceType.ID, "quick_actions_element_container"),
            checkCast("Landroid/view/ViewGroup;", MatchAfterWithin(10)),
            fieldAccess(type = "Landroid/view/ViewGroup;", opcode = Opcode.IPUT_OBJECT, location = MatchAfterWithin(6))
        ))
        val container = binding.instructionMatches.last().fieldReference
        val owner = container.definingClass
        val setter = Fingerprint(definingClass = owner, parameters = listOf("Ljava/lang/Object;", "L"),
            returnType = "L", filters = listOf(opcode(Opcode.CHECK_CAST),
                methodCall(definingClass = owner, returnType = "V", location = MatchAfterImmediately())
            )).instructionMatches.last().getMethodCalled()
        val reset = Fingerprint(definingClass = owner, parameters = listOf("L"), returnType = "V", filters = listOf(
            methodCall(smali = setter.smali),
            fieldAccess(definingClass = owner, type = "Z", opcode = Opcode.IPUT_BOOLEAN, location = MatchAfterWithin(3))
        )).method
        val size = Fingerprint(name = "onLayoutChange", parameters = listOf("Landroid/view/View;") + List(8) { "I" },
            returnType = "V", filters = listOf(
                methodCall(smali = "Landroid/util/Size;-><init>(II)V"),
                fieldAccess(definingClass = owner, opcode = Opcode.IGET_OBJECT, location = MatchAfterWithin(8)),
                methodCall(parameters = listOf("Ljava/lang/Object;"), returnType = "V", location = MatchAfterImmediately())
            ))
        val dimensions = size.instructionMatches[1].fieldReference
        val emit = size.instructionMatches[2].getMethodCalled()
        val current = Fingerprint(definingClass = dimensions.type, parameters = emptyList(),
            returnType = "Ljava/lang/Object;", accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)).method
        val modelType = setter.parameterTypes.single().toString()
        val stored = Fingerprint(definingClass = owner, name = setter.name, parameters = listOf(modelType), returnType = "V",
            filters = listOf(fieldAccess(definingClass = owner, opcode = Opcode.IPUT_OBJECT))).instructionMatches.single().fieldReference
        val model = mutableClassDefBy(stored.type).fields.single { it.type == modelType }
        val controller = mutableClassDefBy(owner)
        val bridge = "$EXTENSION_PACKAGE/playback/QuickActionsController;"
        controller.interfaces.add(bridge)
        controller.addFieldGetter("afterglow_quickActionsView", "Landroid/view/View;", container)
        controller.addMethod("afterglow_refreshQuickActions", "Z", 3, """
            iget-object v1, p0, ${stored.smali}
            if-eqz v1, :unavailable
            iget-object v1, v1, ${model.smali}
            const/4 v0, 0x0
            invoke-virtual { p0, v0 }, ${reset.smali}
            invoke-virtual { p0, v1 }, ${setter.smali}
            iget-object v0, p0, ${dimensions.smali}
            invoke-virtual { v0 }, ${current.smali}
            move-result-object v1
            if-eqz v1, :unavailable
            invoke-virtual { v0, v1 }, ${emit.smali}
            const/4 v0, 0x1
            return v0
            :unavailable
            const/4 v0, 0x0
            return v0
        """)
        val index = binding.instructionMatches.last().index
        val register = (binding.method.implementation!!.instructions[index] as TwoRegisterInstruction).registerB
        binding.method.addInstruction(index + 1,
            "invoke-static/range { v$register .. v$register }, $EXTENSION_PACKAGE/playback/QuickActions;->bind($bridge)V")
        println("Afterglow quick actions: ${setter.smali}, reset ${reset.smali}, dimensions ${dimensions.smali}")
    }
}

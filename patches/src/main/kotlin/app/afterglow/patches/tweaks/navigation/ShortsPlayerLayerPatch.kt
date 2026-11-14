package app.afterglow.patches.tweaks.navigation

import app.afterglow.patches.shared.Constants.COMPATIBILITY_YOUTUBE
import app.afterglow.patches.shared.Constants.EXTENSION_PACKAGE
import app.afterglow.patches.shared.afterglowExtensionPatch
import app.afterglow.patches.shared.invokeRegisters
import app.afterglow.patches.video.addPublicMethod
import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.methodCall
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.resource.ResourceType
import app.morphe.patcher.resourceLiteral
import app.morphe.patcher.util.addInstructionsAtControlFlowLabel

private const val LAYER = "$EXTENSION_PACKAGE/navigation/ShortsPlayerLayer;"
private const val CONTROLLER = "$EXTENSION_PACKAGE/navigation/ShortsPlayerLayer\$Controller;"

/** Preserve native visibility state, the separate decoded video and parent touch dispatch. */
internal val shortsPlayerLayerHookPatch = bytecodePatch {
    compatibleWith(COMPATIBILITY_YOUTUBE)
    dependsOn(afterglowExtensionPatch)
    execute {
        val visibility = Fingerprint(returnType = "V", parameters = emptyList(), filters = listOf(
            resourceLiteral(ResourceType.ID, "reel_player_overlay_container"),
            methodCall(smali = "Larar;->Z(Landroid/view/View;Z)V")
        ), custom = { method, cls -> cls.type == "Lmpo;" && method.name == "Y" })
        val host = visibility.classDef
        require(listOf("S", "o").all { name -> host.fields.any { it.name == name && it.type == "Z" } })
        host.interfaces.add(CONTROLLER)
        host.addPublicMethod("afterglow_refreshLayer", emptyList(), "Z", 2, """
            invoke-virtual { p0 }, Landroid/view/View;->isAttachedToWindow()Z
            move-result v0
            if-eqz v0, :inactive
            invoke-virtual { p0 }, ${host.type}->Y()V
            const/4 v0, 0x1
            return v0
            :inactive
            const/4 v0, 0x0
            return v0
        """)
        val call = visibility.instructionMatches.last()
        val arguments = call.instruction.invokeRegisters
        require(arguments.size == 2)
        val shown = arguments[1]
        visibility.method.addInstructionsAtControlFlowLabel(call.index, """
            ${invokeStatic(shown, method = "$LAYER->visible(Z)Z")}
            move-result v$shown
        """)
        visibility.method.addInstructions(0,
            "invoke-static/range { p0 .. p0 }, $LAYER->observe($CONTROLLER)V")
        println("Afterglow Shorts player layer: ${host.type}->${visibility.method.name}, native visibility preserved")
    }
}

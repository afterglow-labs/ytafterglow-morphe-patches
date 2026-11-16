/*
 * YTAfterglow for Android.
 *
 * Adapted from the RenderNext patch of Morphe patches v2.2.0
 * (https://github.com/MorpheApp/morphe-patches, GPLv3). Modified for YTAfterglow:
 * rewritten for patcher 2.1.0 and only the paths YouTube 21.43.523 has.
 * Not an official Morphe file.
 *
 * Portions of this software are provided "AS IS" by the Morphe software project.
 * Any express or implied warranties, including the implied warranties of
 * merchantability and fitness for a particular purpose, are disclaimed.
 */

package app.afterglow.patches.litho

import app.afterglow.patches.shared.Constants.COMPATIBILITY_YOUTUBE
import app.afterglow.patches.shared.setRegisterFalseAfter
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.returnEarly
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

/**
 * Renders every element with Litho.
 *
 * RenderNext (ElementsView) is Google's alternative to Litho, switched on by A/B flags and by the
 * server per element. Elements it renders never reach the element conversion that the Litho
 * filter hooks, so they could not be hidden. Every way 21.43.523 can choose RenderNext is turned
 * off:
 * 1. The section list's present context gets no ElementsServices (flag 45661418).
 * 2. The server's enable_rendernext field of an Element proto reads as false.
 * 3. The RenderNext field of an element's config reads as false, at every read.
 * 4. No Litho template is converted to RenderNext (the template list of flag 45661551).
 * 5. Home is not opened with the page fragment, which renders the whole page with RenderNext when
 *    the server sends it as one element (flag 1073830434, 21.35+).
 * 6. The hybrid presenter always takes the Litho path, in case an element is RenderNext anyway.
 */
internal val disableRenderNextPatch = bytecodePatch(
    description = "Renders every element with Litho, so Afterglow's Litho filters see all of them.",
) {
    compatibleWith(COMPATIBILITY_YOUTUBE)

    execute {
        // Resolve everything before changing any method.
        val presentContext = RenderNextPresentContextFingerprint.method
        val presentContextFlagResult = RenderNextPresentContextFingerprint.instructionMatches[2].index
        val enablementCheck = RenderNextEnablementCheckFingerprint.method
        val configField = renderNextConfigFieldFingerprint(enablementCheck).instructionMatches.first().fieldReference
        val configReads = renderNextConfigFieldReadFingerprint(configField).matchAll()
        val templateCheck = RenderNextTemplateCheckFingerprint.method
        val pageRoute = PageFragmentRouteFingerprint.method
        val pageRouteFlagResult = PageFragmentRouteFingerprint.instructionMatches[3].index
        val hybridPresenter = HybridElementPresenterFingerprint.method
        val hybridCheck = HybridElementPresenterFingerprint.instructionMatches.first().index

        if (configReads.size != RENDER_NEXT_CONFIG_READS) {
            throw PatchException(
                "Expected $RENDER_NEXT_CONFIG_READS reads of the RenderNext config field $configField, " +
                    "found ${configReads.size}",
            )
        }
        // The RenderNext check is the instance-of whose result the next if-eqz branches on.
        val hybridRegister = hybridPresenter.getInstruction<OneRegisterInstruction>(hybridCheck).registerA
        val afterHybridCheck = hybridPresenter.implementation!!.instructions[hybridCheck + 1]
        if (afterHybridCheck.opcode != Opcode.IF_EQZ ||
            (afterHybridCheck as OneRegisterInstruction).registerA != hybridRegister
        ) {
            throw PatchException("Unexpected RenderNext check in $hybridPresenter")
        }

        // 1. No ElementsServices for the section list.
        presentContext.setRegisterFalseAfter(presentContextFlagResult)

        // 2. Not RenderNext by the Element proto.
        enablementCheck.returnEarly(false)

        // 3. Not RenderNext by the element config.
        configReads.forEach { read ->
            read.method.setRegisterFalseAfter(read.instructionMatches.first().index)
        }

        // 4. No Litho template converted to RenderNext.
        templateCheck.returnEarly(false)

        // 5. Home opens with the browse fragment.
        pageRoute.setRegisterFalseAfter(pageRouteFlagResult)

        // 6. Always present with Litho.
        hybridPresenter.setRegisterFalseAfter(hybridCheck)
    }
}

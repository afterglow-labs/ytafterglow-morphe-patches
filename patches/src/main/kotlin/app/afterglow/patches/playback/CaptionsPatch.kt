/*
 * YTAfterglow for Android.
 *
 * Adapted from the auto captions and start video informer patches of Morphe patches
 * v2.2.0 (https://github.com/MorpheApp/morphe-patches, GPLv3), which were forked from
 * ReVanced patches (https://github.com/ReVanced/revanced-patches, GPLv3). Modified for
 * YTAfterglow: rewritten for patcher 2.1.0, gated by the iOS settings.
 * Not an official Morphe or ReVanced file.
 *
 * Portions of this software are provided "AS IS" by the Morphe software project.
 * Any express or implied warranties, including the implied warranties of
 * merchantability and fitness for a particular purpose, are disclaimed.
 */

package app.afterglow.patches.playback

import app.afterglow.patches.shared.Constants.COMPATIBILITY_YOUTUBE
import app.afterglow.patches.shared.afterglowExtensionPatch
import app.afterglow.patches.video.debugMatch
import app.afterglow.patches.video.installAtStartup
import app.afterglow.patches.video.videoStatePatch
import app.morphe.patcher.Fingerprint
import app.morphe.patcher.InstructionLocation.MatchAfterWithin
import app.morphe.patcher.OpcodesFilter
import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.literal
import app.morphe.patcher.methodCall
import app.morphe.patcher.opcode
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

private const val CAPTIONS_CLASS = "$EXTENSION_PLAYBACK/Captions;"

/** Asks the system captioning settings (CaptioningManager) if captions should show. */
private object SubtitleManagerClassFingerprint : Fingerprint(
    isStatic = true,
    returnType = "Z",
    parameters = listOf("L", "Landroid/view/accessibility/CaptioningManager;"),
    filters = listOf(
        fieldAccess("Ljava/util/concurrent/TimeUnit;->SECONDS:Ljava/util/concurrent/TimeUnit;"),
        methodCall("Landroid/view/accessibility/CaptioningManager;->isEnabled()Z"),
    ),
)

/**
 * Decides at the start of a video if captions start disabled: the boolean the `if-eqz` tests.
 */
internal object SubtitleManagerFingerprint : Fingerprint(
    classFingerprint = SubtitleManagerClassFingerprint,
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "V",
    parameters = listOf("L"),
    filters = listOf(
        string(""),
        methodCall(opcode = Opcode.INVOKE_VIRTUAL, parameters = listOf(), returnType = "Z"),
        opcode(Opcode.IF_EQZ, location = MatchAfterWithin(3)),
    ),
)

/** Tells the player's listeners that a video started (video information loaded). */
internal object StartVideoInformerFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "V",
    filters = OpcodesFilter.opcodesToFilters(Opcode.INVOKE_INTERFACE, Opcode.RETURN_VOID),
    strings = listOf("pc"),
)

/** Shows captions by themselves while the device is muted (20.26+). */
private const val NO_VOLUME_CAPTIONS_FLAG = 45692436L

/**
 * Captions for the iOS setting `disableAutoCaptions` (tweak "captions"). iOS reads YouTube's
 * "turn captions on" hint as "no preference" and turns off the captions that appear by
 * themselves while the device is muted. Here, while the setting is on, captions start disabled
 * when a video starts (the first 150 ms after its information loads, so the viewer's own taps
 * on the captions button still count) and the muted-device captions flag is off.
 */
internal val captionsHookPatch = bytecodePatch(
    description = "Keeps captions from turning on by themselves when YTAfterglow is set to.",
) {
    compatibleWith(COMPATIBILITY_YOUTUBE)
    dependsOn(afterglowExtensionPatch, videoStatePatch)

    execute {
        val subtitleManager = SubtitleManagerFingerprint.method
        val disabledCheck = SubtitleManagerFingerprint.instructionMatches.last().index
        val videoStarted = StartVideoInformerFingerprint.method
        val mutedFlagReads = Fingerprint(filters = listOf(literal(NO_VOLUME_CAPTIONS_FLAG))).matchAll().map { it.method }
        if (mutedFlagReads.isEmpty()) throw PatchException("No read of flag $NO_VOLUME_CAPTIONS_FLAG")

        debugMatch("subtitleManager", subtitleManager)
        debugMatch("videoStarted", videoStarted)
        mutedFlagReads.forEach { debugMatch("mutedCaptionsFlag", it) }

        subtitleManager.apply {
            val register = getInstruction<OneRegisterInstruction>(disabledCheck).registerA
            addInstructions(
                disabledCheck,
                """
                    ${invokeStaticOne(register, "$CAPTIONS_CLASS->disableAutoCaptions(Z)Z")}
                    move-result v$register
                """,
            )
        }

        videoStarted.addInstruction(0, "invoke-static { }, $CAPTIONS_CLASS->videoInformationLoaded()V")

        mutedFlagReads.forEach { method ->
            method.literalIndices(NO_VOLUME_CAPTIONS_FLAG).asReversed().forEach { index ->
                method.overrideFlagAfterLiteral(index, "$CAPTIONS_CLASS->allowMutedAutoCaptions(Z)Z")
            }
        }

        installAtStartup(CAPTIONS_CLASS)
    }
}

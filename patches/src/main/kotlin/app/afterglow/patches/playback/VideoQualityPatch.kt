/*
 * YTAfterglow for Android.
 *
 * Adapted from the video quality part of the video information patch of Morphe patches
 * v2.2.0 (https://github.com/MorpheApp/morphe-patches, GPLv3), which was forked from
 * ReVanced patches (https://github.com/ReVanced/revanced-patches, GPLv3). Modified for
 * YTAfterglow: rewritten for patcher 2.1.0 and YouTube 21.43.523, observe-only list hook,
 * the quality picked through the menu's own setter. Not an official Morphe or ReVanced file.
 *
 * Portions of this software are provided "AS IS" by the Morphe software project.
 * Any express or implied warranties, including the implied warranties of
 * merchantability and fitness for a particular purpose, are disclaimed.
 */

package app.afterglow.patches.playback

import app.afterglow.patches.shared.Constants.COMPATIBILITY_YOUTUBE
import app.afterglow.patches.shared.afterglowExtensionPatch
import app.afterglow.patches.shared.smali
import app.afterglow.patches.video.addPublicMethod
import app.afterglow.patches.video.debugMatch
import app.afterglow.patches.video.installAtStartup
import app.afterglow.patches.video.videoStatePatch
import app.morphe.patcher.Fingerprint
import app.morphe.patcher.InstructionLocation.MatchAfterImmediately
import app.morphe.patcher.InstructionLocation.MatchFirst
import app.morphe.patcher.OpcodesFilter
import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

private const val VIDEO_QUALITY_CLASS = "$EXTENSION_PLAYBACK/VideoQuality;"
private const val QUALITY_OPTION_INTERFACE = "$EXTENSION_PLAYBACK/VideoQuality\$Option;"
private const val QUALITY_MENU_INTERFACE = "$EXTENSION_PLAYBACK/VideoQuality\$Menu;"

/**
 * Constructor of a video quality: resolution, ..., name ("1080p60", "720p"), ..., format ids.
 *
 * 21.43.523: `Laley;-><init>(ILbbvv;Ljava/lang/String;ZLazag;)V`; the name is `Laley;->b`, the
 * resolution `Laley;->a`.
 */
internal object VideoQualityFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR),
    parameters = listOf("I", "L", "Ljava/lang/String;", "Z", "L"),
)

/**
 * Gives the quality menu item the qualities of the playing video (largest first, 0 is Auto) and
 * the selected index.
 *
 * 21.43.523: `Lobm;->l([Laley;IZ)V` (classes.dex; 11 registers, p0 is v7).
 */
internal object VideoQualitySetterFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "V",
    parameters = listOf("[L", "I", "Z"),
    filters = OpcodesFilter.opcodesToFilters(
        Opcode.IF_EQZ,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.IPUT_BOOLEAN,
    ),
    strings = listOf("menu_item_video_quality"),
)

/**
 * Sets the quality menu: the quality dialog (`Lobm;->f:Lqqz;`) gets the menu
 * (`Lqqz;->av:Larus;`), whose `b(Laley;)V` selects a quality as a tap in the menu does.
 *
 * 21.43.523: `Lobm;->o(Larus;)V`.
 */
internal object SetVideoQualityMenuFingerprint : Fingerprint(
    classFingerprint = VideoQualitySetterFingerprint,
    returnType = "V",
    parameters = listOf("L"),
    filters = listOf(
        fieldAccess(opcode = Opcode.IGET_OBJECT, definingClass = "this", location = MatchFirst()),
        fieldAccess(opcode = Opcode.IPUT_OBJECT, location = MatchAfterImmediately()),
        fieldAccess(opcode = Opcode.IGET_OBJECT, definingClass = "this", location = MatchAfterImmediately()),
    ),
)

/**
 * Default quality for the iOS settings `wiFiQualityIndex` and `cellQualityIndex` (tweak
 * "video-quality"). The quality menu item reports every quality list of the playing video to
 * the extension (observe-only), with the menu that selects a quality. The extension picks the
 * quality as iOS's autoQuality does and selects it through the menu once per video, as if the
 * viewer had tapped it.
 */
internal val videoQualityHookPatch = bytecodePatch(
    description = "Selects the default video quality YTAfterglow is set to for Wi-Fi and mobile data.",
) {
    compatibleWith(COMPATIBILITY_YOUTUBE)
    dependsOn(afterglowExtensionPatch, videoStatePatch)

    execute {
        val qualityClass = VideoQualityFingerprint.classDef
        val setter = VideoQualitySetterFingerprint.method
        val dialogField = SetVideoQualityMenuFingerprint.instructionMatches[0].fieldReference
        val menuField = SetVideoQualityMenuFingerprint.instructionMatches[1].fieldReference
        val menuClass = mutableClassDefBy(menuField.type)
        debugMatch("qualitySetter", setter)
        debugMatch("qualityMenu", SetVideoQualityMenuFingerprint.method)
        debugMatch("quality", VideoQualityFingerprint.method)

        if (setter.parameterTypes.first().toString() != "[${qualityClass.type}") {
            throw PatchException("Unexpected quality list ${setter.parameterTypes.first()} for ${qualityClass.type}")
        }
        val nameField = qualityClass.fields.singleOrNull { it.type == "Ljava/lang/String;" && !AccessFlags.STATIC.isSet(it.accessFlags) }
            ?: throw PatchException("${qualityClass.type} has no single name field")
        val resolutionField = qualityClass.fields.singleOrNull { it.type == "I" && !AccessFlags.STATIC.isSet(it.accessFlags) }
            ?: throw PatchException("${qualityClass.type} has no single resolution field")
        val selectQuality = menuClass.methods.singleOrNull { method ->
            method.returnType == "V" && method.parameterTypes.map { it.toString() } == listOf(qualityClass.type)
        } ?: throw PatchException("${menuClass.type} has no single quality setter")

        qualityClass.apply {
            interfaces.add(QUALITY_OPTION_INTERFACE)
            addPublicMethod(
                "afterglow_getQualityName", listOf(), "Ljava/lang/String;", 2,
                """
                    iget-object v0, p0, $type->${nameField.name}:Ljava/lang/String;
                    return-object v0
                """,
            )
            addPublicMethod(
                "afterglow_getResolution", listOf(), "I", 2,
                """
                    iget v0, p0, $type->${resolutionField.name}:I
                    return v0
                """,
            )
        }

        menuClass.apply {
            interfaces.add(QUALITY_MENU_INTERFACE)
            addPublicMethod(
                "afterglow_selectQuality", listOf(QUALITY_OPTION_INTERFACE), "V", 2,
                """
                    check-cast p1, ${qualityClass.type}
                    invoke-virtual { p0, p1 }, ${menuClass.type}->${selectQuality.name}(${qualityClass.type})V
                    return-void
                """,
            )
        }

        // The list and the menu (when the dialog has one), before the menu item uses them.
        // v0 and v1 are locals, free at the start; p0 must fit the 4-bit field reads.
        setter.apply {
            val parameterRegisters = parameterTypes.size + 1
            val locals = implementation!!.registerCount - parameterRegisters
            if (locals < 2 || locals > 15) throw PatchException("Unexpected registers in $this")
            addInstructionsWithLabels(
                0,
                """
                    iget-object v0, p0, ${dialogField.smali}
                    if-eqz v0, :afterglow_no_menu
                    iget-object v0, v0, ${menuField.smali}
                    :afterglow_no_menu
                    move-object/from16 v1, p1
                    invoke-static { v1, v0 }, $VIDEO_QUALITY_CLASS->onQualities([Ljava/lang/Object;Ljava/lang/Object;)V
                """,
            )
        }

        installAtStartup(VIDEO_QUALITY_CLASS)
    }
}

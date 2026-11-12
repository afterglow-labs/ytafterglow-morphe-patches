package app.afterglow.patches.shared

import app.afterglow.patches.shared.Constants.COMPATIBILITY_YOUTUBE
import app.afterglow.patches.shared.Constants.EXTENSION
import app.afterglow.patches.shared.Constants.EXTENSION_PACKAGE
import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.patch.bytecodePatch

internal object MainActivityOnCreateFingerprint : Fingerprint(
    definingClass = "Lcom/google/android/apps/youtube/app/watchwhile/MainActivity;",
    name = "onCreate",
    returnType = "V",
    parameters = listOf("Landroid/os/Bundle;"),
)

/**
 * Merges the Afterglow extension and tells it about YouTube's main activity,
 * so every other patch can rely on {@code Afterglow.context()}.
 */
val afterglowExtensionPatch = bytecodePatch(
    description = "Loads the YTAfterglow extension.",
) {
    compatibleWith(COMPATIBILITY_YOUTUBE)

    extendWith(EXTENSION)

    execute {
        // onCreate uses more than 16 registers, so p0 can be above v15: the
        // range form reaches any register, plain invoke-static cannot.
        MainActivityOnCreateFingerprint.method.addInstruction(
            0,
            "invoke-static/range { p0 .. p0 }, $EXTENSION_PACKAGE/Afterglow;->onMainActivityCreate(Landroid/app/Activity;)V",
        )
    }
}

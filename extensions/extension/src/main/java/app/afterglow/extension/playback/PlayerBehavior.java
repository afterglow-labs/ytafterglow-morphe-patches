package app.afterglow.extension.playback;

import app.afterglow.extension.InstalledTweaks;
import app.afterglow.extension.settings.LiteMode;

/** Settings gates for the native seek and cinematic renderer boundaries. */
public final class PlayerBehavior {
    private PlayerBehavior() {}

    public static boolean tapToSeek() {
        return InstalledTweaks.has("player-gestures") && LiteMode.effectiveBool("tapToSeek");
    }

    public static boolean disableAmbientMode() {
        return InstalledTweaks.has("ambient-mode") && LiteMode.effectiveBool("disableAmbientMode");
    }
}

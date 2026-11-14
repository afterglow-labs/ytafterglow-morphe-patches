package app.afterglow.extension.playback;

import app.afterglow.extension.InstalledTweaks;
import app.afterglow.extension.settings.LiteMode;

/** Settings gates for the native seek and cinematic renderer boundaries. */
public final class PlayerBehavior {
    private PlayerBehavior() {}

    public static boolean tapToSeek() {
        return InstalledTweaks.has("player-gestures") && LiteMode.effectiveBool("tapToSeek");
    }

    /** Native classification keeps ordinary seeking and two-finger chapter skips independent. */
    public static boolean blockDoubleTap(boolean chapterSeek) {
        return InstalledTweaks.has("player-gestures") && LiteMode.effectiveBool(
                chapterSeek ? "noTwoFingerSnapToChapter" : "noDoubleTapToSeek");
    }

    public static boolean disableFreeZoom() {
        return InstalledTweaks.has("player-gestures") && LiteMode.effectiveBool("noFreeZoom");
    }

    /** Use YouTube's own fill-only path, without changing its saved zoom capability. */
    public static boolean allowFreeZoom(boolean nativeEnabled) {
        return nativeEnabled && !disableFreeZoom();
    }

    public static boolean disableAmbientMode() {
        return InstalledTweaks.has("ambient-mode") && LiteMode.effectiveBool("disableAmbientMode");
    }
}

package app.afterglow.extension.playback;

import app.afterglow.extension.InstalledTweaks;
import app.afterglow.extension.settings.LiteMode;

/** Settings gates for the native seek and cinematic renderer boundaries. */
public final class PlayerBehavior {
    private PlayerBehavior() {}

    public static boolean rememberLoop() {
        return InstalledTweaks.has("loop") && LiteMode.effectiveBool("rememberLoop");
    }

    public static boolean showEndTime() {
        return InstalledTweaks.has("video-end-time") && LiteMode.effectiveBool("videoEndTime");
    }

    public static boolean portraitFullscreen() {
        return InstalledTweaks.has("fullscreen") && LiteMode.effectiveBool("portraitFullscreen");
    }

    public static boolean autoFullscreen() {
        return InstalledTweaks.has("fullscreen") && LiteMode.effectiveBool("autoFullscreen");
    }

    public static boolean exitFullscreen() {
        return InstalledTweaks.has("fullscreen") && LiteMode.effectiveBool("exitFullscreen");
    }

    public static boolean tapToSeek() {
        return InstalledTweaks.has("player-gestures") && LiteMode.effectiveBool("tapToSeek");
    }

    /** Scrub release uses the finger's position, independently of chapter double-taps. */
    public static boolean disableChapterSnap() {
        return InstalledTweaks.has("player-gestures") && LiteMode.effectiveBool("dontSnapToChapter");
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

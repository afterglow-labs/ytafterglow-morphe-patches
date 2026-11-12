/*
 * YTAfterglow for Android.
 *
 * Adapted from the AutoCaptionsPatch class of Morphe patches v2.2.0
 * (https://github.com/MorpheApp/morphe-patches, GPLv3). Modified for YTAfterglow:
 * driven by the iOS settings. Not an official Morphe file.
 */

package app.afterglow.extension.playback;

import android.os.SystemClock;

import java.util.concurrent.atomic.AtomicLong;

import app.afterglow.extension.Afterglow;
import app.afterglow.extension.InstalledTweaks;
import app.afterglow.extension.settings.LiteMode;
import app.afterglow.extension.video.VideoState;

/**
 * iOS "captions" tweak.
 *
 * <p>{@code disableAutoCaptions} ("Disable auto captions: prevents automatic activation of
 * captions"). While on, captions start disabled when a video starts, and the captions YouTube
 * turns on by itself while the device is muted stay off. During the first 150 ms after a video's
 * information loads YouTube's start decision reads as "disabled"; after that the viewer's own
 * taps on the captions button are kept, as on iOS where the captions button is unaffected.
 */
@SuppressWarnings("unused")
public final class Captions {
    private static final String TWEAK = "captions";
    /** Window after a video's information loads during which the start decision is forced. */
    private static final long START_WINDOW_MS = 150;

    /** End of the start window; MAX_VALUE while a video loads, before its information. */
    private static final AtomicLong START_WINDOW_END = new AtomicLong(Long.MAX_VALUE);
    private static boolean installed;

    private Captions() {}

    /** Injection point, at startup. */
    public static synchronized void install() {
        if (installed) return;
        installed = true;
        try {
            VideoState.addListener(new VideoState.Listener() {
                @Override
                public void onPlayerCreated() {
                    START_WINDOW_END.set(Long.MAX_VALUE);
                }
            });
        } catch (Throwable error) {
            Afterglow.error("Captions.install", error);
        }
    }

    private static boolean disableAutoCaptionsOn() {
        return InstalledTweaks.has(TWEAK) && LiteMode.effectiveBool("disableAutoCaptions");
    }

    /** Injection point: a video's information loaded; opens the start window once. */
    public static void videoInformationLoaded() {
        try {
            START_WINDOW_END.compareAndSet(Long.MAX_VALUE, SystemClock.uptimeMillis() + START_WINDOW_MS);
        } catch (Throwable error) {
            Afterglow.error("Captions.videoInformationLoaded", error);
        }
    }

    /** Injection point: YouTube's "captions start disabled" decision. */
    public static boolean disableAutoCaptions(boolean original) {
        try {
            if (!disableAutoCaptionsOn()) return original;
            if (SystemClock.uptimeMillis() >= START_WINDOW_END.get()) return original;
            return true;
        } catch (Throwable error) {
            Afterglow.error("Captions.disableAutoCaptions", error);
            return original;
        }
    }

    /** Injection point: the flag that shows captions by themselves on a muted device. */
    public static boolean allowMutedAutoCaptions(boolean original) {
        try {
            return original && !disableAutoCaptionsOn();
        } catch (Throwable error) {
            Afterglow.error("Captions.allowMutedAutoCaptions", error);
            return original;
        }
    }
}

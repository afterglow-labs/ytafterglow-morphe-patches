package app.afterglow.extension.playback;

import app.afterglow.extension.Afterglow;
import app.afterglow.extension.InstalledTweaks;
import app.afterglow.extension.settings.AfterglowPrefs;

/**
 * iOS {@code autoplayMode} ("Autoplay": 0 Default, 1 Off, 2 Off + hidden; tweak "autoplay").
 * Option 2 hides the player's Autoplay switch, read when the player controls are built.
 * (iOS's "Off" makes an opened video start paused; that part has no Android hook yet.)
 */
@SuppressWarnings("unused")
public final class Autoplay {
    private Autoplay() {}

    /** Injection point: true skips inflating the Autoplay switch. */
    public static boolean hideAutoplaySwitch() {
        try {
            return InstalledTweaks.has("autoplay") && Math.min(Math.max(AfterglowPrefs.getInt("autoplayMode"), 0), 2) == 2;
        } catch (Throwable error) {
            Afterglow.error("Autoplay.hideAutoplaySwitch", error);
            return false;
        }
    }
}

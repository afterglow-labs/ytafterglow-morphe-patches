package app.afterglow.extension.settings;

import android.graphics.Color;

/**
 * Reads the theme's semantic colors from the same {@code theme_*} keys the iOS
 * theme store uses (YTAGTheme.m YTAGThemeLegacyKeyForRole). This is the single
 * decoder for those keys; the settings summaries and the settings palette both
 * read through it.
 *
 * <p>Storage: iOS keeps each color as an archived UIColor blob. Theme Studio is not
 * ported to Android yet, so a color is read from an ARGB integer (Integer/Long) or
 * a "#RRGGBB" / "#AARRGGBB" string. A missing or unreadable value means "use
 * YouTube's native color", as on iOS.
 */
public final class ThemeColors {
    public static final String BACKGROUND = "theme_background";
    public static final String NAVIGATION = "theme_navBar";
    public static final String TEXT_PRIMARY = "theme_textPrimary";
    public static final String TEXT_SECONDARY = "theme_textSecondary";
    public static final String ACCENT = "theme_accent";
    public static final String TAB_ICONS = "theme_tabBarIcons";
    public static final String GRADIENT_START = "theme_gradientStart";
    public static final String GRADIENT_END = "theme_gradientEnd";
    public static final String GLOW = "theme_glowColor";

    private ThemeColors() {}

    /** The stored color as ARGB, or null when the key holds no readable color. */
    public static Integer color(String key) {
        return decode(AfterglowPrefs.snapshot().get(key));
    }

    public static Integer decode(Object value) {
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Long) return (int) (long) (Long) value;
        if (value instanceof String) {
            String text = ((String) value).trim();
            if (text.matches("#[0-9A-Fa-f]{6}")) return 0xFF000000 | Integer.parseInt(text.substring(1), 16);
            if (text.matches("#[0-9A-Fa-f]{8}")) return (int) Long.parseLong(text.substring(1), 16);
        }
        return null;
    }

    /** "#RRGGBB" for a stored color (iOS themeHexFromColor), or null. */
    public static String hex(String key) {
        Integer color = color(key);
        return color == null ? null : String.format(java.util.Locale.ROOT, "#%06X", color & 0xFFFFFF);
    }

    /** YES when both gradient colors are set (iOS gradientEnabled). */
    public static boolean gradientEnabled() {
        return color(GRADIENT_START) != null && color(GRADIENT_END) != null;
    }

    /**
     * iOS pageStyleOverride: when the theme owns the background (a background color or a
     * gradient) its foundation decides light (0) or dark (1); otherwise -1, follow the app.
     * Android derives the foundation from the background's luminance.
     */
    public static int pageStyleOverride() {
        Integer background = color(BACKGROUND);
        if (background == null && gradientEnabled()) background = color(GRADIENT_START);
        if (background == null) return -1;
        return luminance(background) > 0.5 ? 0 : 1;
    }

    /** WCAG relative luminance (YTAGContrastPolicy). */
    public static double luminance(int color) {
        return 0.2126 * channel(Color.red(color)) + 0.7152 * channel(Color.green(color)) + 0.0722 * channel(Color.blue(color));
    }

    private static double channel(int value) {
        double c = value / 255.0;
        return c <= 0.03928 ? c / 12.92 : Math.pow((c + 0.055) / 1.055, 2.4);
    }

    /** WCAG contrast ratio between two opaque colors. */
    public static double contrast(int a, int b) {
        double la = luminance(a);
        double lb = luminance(b);
        return (Math.max(la, lb) + 0.05) / (Math.min(la, lb) + 0.05);
    }

    /** Black or white, whichever reads better on {@code background} (YTAGContrastReadableForeground). */
    public static int blackOrWhite(int background) {
        return contrast(Color.BLACK, background) >= contrast(Color.WHITE, background) ? Color.BLACK : Color.WHITE;
    }
}

/*
 * Neutral Litho color list adapted from Morphe's YouTube ThemePatch (GPLv3),
 * https://github.com/MorpheApp/morphe-patches . See NOTICE.
 * Portions of this software are provided "AS IS" by the Morphe software project.
 * Any express or implied warranties, including the implied warranties of
 * merchantability and fitness for a particular purpose, are disclaimed.
 */
package app.afterglow.extension.settings;

/** Pure color policy shared by the resource and Litho adapters. Unowned roles stay native. */
public final class NativeThemeColors {
    private NativeThemeColors() {}

    /** Match the verified LIGHT/DARK enum names; never write YouTube's saved Appearance setting. */
    public static Enum<?> appearance(ThemePalette palette, Enum<?> original) {
        if (original == null || palette.pageStyleOverride() == -1) return original;
        String name = palette.lightFoundation ? "LIGHT" : "DARK";
        for (Enum<?> value : original.getDeclaringClass().getEnumConstants()) {
            if (name.equals(value.name())) return value;
        }
        return original;
    }

    public static Integer role(ThemePalette palette, ThemeRole role) {
        Integer color = palette.color(role);
        if (color != null) return color;
        // A background-only import still needs the text of its chosen iOS foundation.
        if (palette.pageStyleOverride() != -1) {
            if (role == ThemeRole.PRIMARY_TEXT) return palette.lightFoundation ? 0xFF0F0F0F : 0xFFF1F1F1;
            if (role == ThemeRole.SECONDARY_TEXT) return palette.lightFoundation ? 0xFF606060 : 0xFFAAAAAA;
        }
        return null;
    }

    public static int withOpacity(int color, double opacity) {
        return ((int) Math.round((color >>> 24) * opacity) << 24) | (color & 0xFFFFFF);
    }

    /** Only the verified neutral Litho background colors, never video Paints or arbitrary RGB. */
    public static int lithoBackground(ThemePalette palette, int original, boolean nativeDark) {
        Integer color = role(palette, ThemeRole.BACKGROUND);
        if (color == null) return original;
        boolean background = nativeDark ? switch (original) {
            case 0xFF282828, 0xFF212121, 0xFF181818, 0xFF0F0F0F, 0xFA212121,
                    0xFF222225, 0xFF0E0E10, 0xFF09090A -> true;
            default -> false;
        } : original == 0xFFFFFFFF || original == 0xFFF9F9F9 || original == 0xFAFFFFFF;
        return background ? withOpacity(color, (original >>> 24) / 255.0) : original;
    }
}

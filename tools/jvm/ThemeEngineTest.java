package app.afterglow.extension.settings;

import android.content.SharedPreferences;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static app.afterglow.extension.settings.ThemeRole.*;

/** Behavior tests of the real color engine with Android's preference storage replaced in memory. */
public final class ThemeEngineTest {
    private static int checks;
    private static int reads;
    private static int writes;
    private static final Map<String, Object> stored = new HashMap<>();

    public static void main(String[] args) throws Exception {
        Field prefs = AfterglowPrefs.class.getDeclaredField("prefs");
        prefs.setAccessible(true);
        prefs.set(null, memoryPreferences());
        decoding();
        foundations();
        derivedColors();
        catalog();
        transactionsAndCache();
        System.out.println("PASS theme engine: " + checks + " checks");
    }

    private static void decoding() {
        eq("RGB", 0xFF123456, ThemeColors.decode(" #123456 "));
        eq("ARGB", 0x78123456, ThemeColors.decode("#78123456"));
        eq("transparent is defined", 0, ThemeColors.decode("#00000000"));
        eq("signed ARGB", 0xFFABCDEF, ThemeColors.decode(0xFFABCDEF));
        eq("unsigned long ARGB", 0xFFABCDEF, ThemeColors.decode(0xFFABCDEFL));
        for (Object invalid : new Object[] {null, "red", "#fff", "#12345G", "#123456789", true, new byte[] {1, 2}})
            eq("invalid color ignored: " + invalid, null, ThemeColors.decode(invalid));
        eq("white WCAG luminance", 1.0, ThemeColors.luminance(0xFFFFFFFF));
        eq("black/white contrast", 21.0, ThemeColors.contrast(0xFF000000, 0xFFFFFFFF));
        eq("readable on white", 0xFF000000, ThemeColors.blackOrWhite(0xFFFFFFFF));
        eq("readable on black", 0xFFFFFFFF, ThemeColors.blackOrWhite(0xFF000000));
    }

    private static void foundations() {
        eq("empty palette follows native", -1, ThemePalette.system().pageStyleOverride());
        for (ThemeRole role : ThemeRole.values()) eq("system has no derived " + role, null, ThemePalette.system().color(role));
        eq("accent alone follows native", -1, palette(ACCENT, 0xFFFFFFFF).pageStyleOverride());
        // 0.56 perceived brightness is light on iOS, despite a WCAG luminance below 0.5.
        eq("Rec.601 foundation", 0, palette(BACKGROUND, 0xFF909090).pageStyleOverride());
        eq("below threshold", 1, palette(BACKGROUND, 0xFF8C8C8C).pageStyleOverride());
        eq("above threshold", 0, palette(BACKGROUND, 0xFF8D8D8D).pageStyleOverride());
        ThemePalette gradient = palette(BACKGROUND, 0xFF000000, GRADIENT_START, 0xFFFFFFFF, GRADIENT_END, 0xFF333333);
        eq("gradient overrides background foundation", 0, gradient.pageStyleOverride());
        eq("gradient endpoints averaged", 1, palette(GRADIENT_START, 0xFFFFFFFF, GRADIENT_END, 0xFF000000).pageStyleOverride());
        eq("incomplete gradient does not own page", -1, palette(GRADIENT_START, 0xFFFFFFFF).pageStyleOverride());
        eq("end-only uses background", 1, palette(BACKGROUND, 0xFF000000, GRADIENT_END, 0xFFFFFFFF).pageStyleOverride());
        eq("defined transparent background owns page", 1, palette(BACKGROUND, 0).pageStyleOverride());
    }

    private static void derivedColors() {
        ThemePalette p = palette(BACKGROUND, 0xFF010203, NAVIGATION_SURFACE, 0xFF040506,
                PRIMARY_TEXT, 0xFFEEEEEE, SECONDARY_TEXT, 0x10123456, ACCENT, 0xFF11AA77,
                TAB_ICON, 0xFF8899AA, SEEK_PLAYED, 0xFF123456, SEEK_LIVE, 0xFF654321);
        eq("elevated prefers navigation", 0xFF040506, p.color(ELEVATED_SURFACE));
        eq("elevated background fallback", 0xFF010203, palette(BACKGROUND, 0xFF010203).color(ELEVATED_SURFACE));
        eq("border replaces alpha", 0x38123456, p.color(BORDER));
        eq("primary border fallback", 0x2EEEEEEE, palette(PRIMARY_TEXT, 0xFFEEEEEE).color(BORDER));
        eq("overlay backdrop", 0x9E000000, p.color(OVERLAY_BACKDROP));
        eq("dark shadow", 0x8C000000, p.color(SHADOW));
        eq("light shadow", 0x38000000, palette(BACKGROUND, 0xFFFFFFFF).color(SHADOW));
        eq("buffered legacy gray", 0x99A6A6A6, p.color(SEEK_BUFFERED));
        eq("buffered needs played override", null, palette(SEEK_LIVE, 0xFF123456).color(SEEK_BUFFERED));
        eq("live override retained", 0xFF654321, p.color(SEEK_LIVE));
        eq("scrubber derives played", 0xFF123456, p.color(SEEK_SCRUBBER));
        eq("live scrubber prefers EXPLICIT live over derived scrubber", 0xFF654321, p.color(SEEK_SCRUBBER_LIVE));
        eq("explicit scrubber first", 0xFF000011,
                palette(SEEK_PLAYED, 0xFF123456, SEEK_LIVE, 0xFF654321, SEEK_SCRUBBER, 0xFF000011).color(SEEK_SCRUBBER_LIVE));
        eq("focus accent", 0xFF11AA77, p.color(FOCUS));
        eq("focus icon fallback", 0xFF8899AA, palette(TAB_ICON, 0xFF8899AA).color(FOCUS));
        eq("derived not explicit", null, p.explicit(SEEK_SCRUBBER));
        eq("no guessed navigation color", null, palette(BACKGROUND, 0xFF123456).color(NAVIGATION_SURFACE));
        for (ThemeRole role : ThemeRole.values()) {
            eq("explicit " + role + " wins", 0x00102030, palette(role, 0x00102030).color(role));
        }
        try {
            p.explicitColors().put(ACCENT, 0);
            throw new AssertionError("mutable palette");
        } catch (UnsupportedOperationException expected) { checks++; }
    }

    private static void catalog() {
        eq("all iOS presets", 30, ThemeCatalog.PRESETS.size());
        for (ThemePalette preset : ThemeCatalog.PRESETS) {
            Map<ThemeRole, Integer> colors = new EnumMap<>(preset.explicitColors());
            ThemePalette resolved = ThemePalette.fromColors(colors);
            eq("recognize " + preset.identifier, preset.identifier, resolved.identifier);
            eq("preserve declared foundation " + preset.identifier, preset.lightFoundation, resolved.lightFoundation);
            colors.put(GLOW, 0xFF445566);
            eq("glow does not change identity " + preset.identifier, preset.identifier, ThemePalette.fromColors(colors).identifier);
            colors.put(BACKGROUND, preset.explicit(BACKGROUND) ^ 1);
            eq("edited preset becomes custom " + preset.identifier, "custom", ThemePalette.fromColors(colors).identifier);
        }
        eq("OLED literal black", 0xFF000000, ThemeCatalog.find("oled-dark").color(BACKGROUND));
        eq("no invented presets", null, ThemeCatalog.find("missing"));
    }

    private static void transactionsAndCache() {
        // Listener predates ThemeStore initialization: reads in callbacks must still be fresh.
        final String[] observed = {null};
        final int[] notifications = {0};
        AfterglowPrefs.Listener listener = key -> {
            observed[0] = ThemeStore.snapshot().palette.identifier;
            notifications[0]++;
        };
        AfterglowPrefs.addListener(listener);
        ThemeStore.Snapshot initial = ThemeStore.snapshot();
        int readCount = reads;
        for (int i = 0; i < 100; i++) {
            ThemeColors.color(ThemeColors.BACKGROUND);
            ThemeColors.gradientEnabled();
            ThemeColors.pageStyleOverride();
        }
        eq("drawing never recopies preferences", readCount, reads);
        AfterglowPrefs.setInt("wiFiQualityIndex", 4);
        eq("unrelated pref retains palette revision", initial, ThemeStore.snapshot());
        AfterglowPrefs.setInt("theme_fontMode", 3);
        AfterglowPrefs.setInt("theme_glowStrengthCustom", 71);
        AfterglowPrefs.setString("theme_savedThemes", "library retained");
        AfterglowPrefs.setString("theme_semanticTheme", "old identity");
        AfterglowPrefs.setInt("theme_glowColor", 0xFF0099FF);
        int writeCount = writes;
        int noticeCount = notifications[0];
        ThemeStore.applyPreset("afterglow-1");
        eq("preset one storage write", writeCount + 1, writes);
        eq("preset one notification", noticeCount + 1, notifications[0]);
        eq("listener sees complete new preset", "afterglow-1", observed[0]);
        eq("unrelated pref retained", 4, AfterglowPrefs.getInt("wiFiQualityIndex"));
        eq("typography retained", 3, AfterglowPrefs.getInt("theme_fontMode"));
        eq("glow preset applied", true, AfterglowPrefs.getBool("theme_glowEnabled"));
        eq("old glow override cleared", false, AfterglowPrefs.contains("theme_glowColor"));
        eq("old semantic record cleared", false, AfterglowPrefs.contains("theme_semanticTheme"));
        ThemeStore.Snapshot before = ThemeStore.snapshot();
        ThemeStore.applyPreset("afterglow-1");
        eq("same palette does not increment revision", before, ThemeStore.snapshot());
        ThemeStore.applyPreset("oled-dark");
        eq("preset change increments revision", before.revision + 1, ThemeStore.snapshot().revision);
        eq("solid preset removes gradient", false, ThemeColors.gradientEnabled());
        eq("preset disables glow", false, AfterglowPrefs.getBool("theme_glowEnabled"));
        eq("legacy read uses cached explicit color", 0xFF000000, ThemeColors.color("theme_background"));
        before = ThemeStore.snapshot();
        AfterglowPrefs.setString("theme_background", "#909090");
        eq("legacy write invalidates snapshot", before.revision + 1, ThemeStore.snapshot().revision);
        eq("legacy custom foundation", 0, ThemeColors.pageStyleOverride());
        AfterglowPrefs.replaceAll(Map.of("theme_background", "#112233", "backgroundPlayback", true));
        eq("import invalidates palette", 0xFF112233, ThemeColors.color("theme_background"));
        AfterglowPrefs.setString("theme_background", "malformed");
        eq("bad import returns native", "system", ThemeStore.snapshot().palette.identifier);

        AfterglowPrefs.setInt("theme_fontMode", 3);
        AfterglowPrefs.setInt("theme_glowStrengthCustom", 71);
        AfterglowPrefs.setString("theme_savedThemes", "library retained");
        ThemeStore.applyPreset("afterglow-1");
        AfterglowPrefs.setInt("theme_glowColor", 0xFF00AA33);
        ThemeStore.resetToSystem();
        eq("reset system", "system", ThemeStore.snapshot().palette.identifier);
        for (ThemeRole role : ThemeRole.values()) if (role.legacyKey != null)
            eq("reset removes " + role, false, AfterglowPrefs.contains(role.legacyKey));
        eq("reset removes glow switch", false, AfterglowPrefs.contains("theme_glowEnabled"));
        eq("reset keeps typography", 3, AfterglowPrefs.getInt("theme_fontMode"));
        eq("reset keeps glow tuning", 71, AfterglowPrefs.getInt("theme_glowStrengthCustom"));
        eq("reset keeps saved library", "library retained", AfterglowPrefs.getString("theme_savedThemes"));
        eq("reset keeps playback", true, AfterglowPrefs.getBool("backgroundPlayback"));
        before = ThemeStore.snapshot();
        writeCount = writes;
        try { ThemeStore.applyPreset("no-such-theme"); throw new AssertionError("accepted unknown preset"); }
        catch (IllegalArgumentException expected) { checks++; }
        eq("invalid preset does not write", writeCount, writes);
        eq("invalid preset retains palette", before, ThemeStore.snapshot());
        try { AfterglowPrefs.update(Map.of("theme_background", 12, "bad", new Object()));
            throw new AssertionError("accepted invalid preference"); }
        catch (IllegalArgumentException expected) { checks++; }
        eq("invalid transaction does not write", writeCount, writes);
        AfterglowPrefs.resetAll();
        eq("whole prefs reset invalidates", "system", ThemeStore.snapshot().palette.identifier);
        AfterglowPrefs.removeListener(listener);
    }

    private static ThemePalette palette(Object... pairs) {
        Map<ThemeRole, Integer> colors = new EnumMap<>(ThemeRole.class);
        for (int i = 0; i < pairs.length; i += 2) colors.put((ThemeRole) pairs[i], (Integer) pairs[i + 1]);
        return ThemePalette.fromColors(colors);
    }

    private static void eq(String message, Object expected, Object actual) {
        checks++;
        if (!Objects.equals(expected, actual)) throw new AssertionError(message + ": expected " + expected + ", got " + actual);
    }

    private static SharedPreferences memoryPreferences() {
        return (SharedPreferences) Proxy.newProxyInstance(ThemeEngineTest.class.getClassLoader(),
                new Class<?>[] {SharedPreferences.class}, (proxy, method, args) -> {
                    String name = method.getName();
                    if (name.equals("getAll")) { reads++; return new HashMap<>(stored); }
                    if (name.equals("contains")) return stored.containsKey(args[0]);
                    if (name.startsWith("get")) return stored.getOrDefault(args[0], args[1]);
                    if (name.equals("edit")) return editor();
                    return null;
                });
    }

    private static SharedPreferences.Editor editor() {
        Map<String, Object> pending = new HashMap<>();
        boolean[] clear = {false};
        return (SharedPreferences.Editor) Proxy.newProxyInstance(ThemeEngineTest.class.getClassLoader(),
                new Class<?>[] {SharedPreferences.Editor.class}, (proxy, method, args) -> {
                    String name = method.getName();
                    if (name.startsWith("put")) pending.put((String) args[0], args[1]);
                    else if (name.equals("remove")) pending.put((String) args[0], null);
                    else if (name.equals("clear")) clear[0] = true;
                    else if (name.equals("apply") || name.equals("commit")) {
                        if (clear[0]) stored.clear();
                        for (Map.Entry<String, Object> entry : pending.entrySet()) {
                            if (entry.getValue() == null) stored.remove(entry.getKey());
                            else stored.put(entry.getKey(), entry.getValue());
                        }
                        writes++;
                        return name.equals("commit") ? true : null;
                    }
                    return proxy;
                });
    }
}

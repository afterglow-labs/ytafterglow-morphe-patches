package app.afterglow.extension.settings;

import java.util.HashMap;
import java.util.Map;

/**
 * Shared color store for Afterglow views and future YouTube adapters. One immutable
 * snapshot per color revision; drawing never copies SharedPreferences or resolves presets.
 * Scalar glow/typography controls and the saved semantic-theme library are not ported here.
 */
public final class ThemeStore {
    public static final class Snapshot {
        public final long revision;
        public final ThemePalette palette;
        private Snapshot(long revision, ThemePalette palette) {
            this.revision = revision;
            this.palette = palette;
        }
    }

    private static volatile Snapshot current;
    private static volatile long preferencesRevision = -1;

    private ThemeStore() {}

    public static Snapshot snapshot() {
        long revision = AfterglowPrefs.revision();
        if (preferencesRevision == revision) {
            Snapshot snapshot = current;
            if (snapshot != null) return snapshot;
        }
        synchronized (ThemeStore.class) {
            // Do not cache startup defaults before Android has supplied an application context.
            if (!AfterglowPrefs.available()) return new Snapshot(0, ThemePalette.system());
            revision = AfterglowPrefs.revision();
            if (current == null || preferencesRevision != revision) {
                Map<String, ?> values;
                do {
                    revision = AfterglowPrefs.revision();
                    values = AfterglowPrefs.snapshot();
                } while (revision != AfterglowPrefs.revision());
                ThemePalette palette = ThemePalette.fromPreferences(values);
                if (current == null || !current.palette.equals(palette)) {
                    current = new Snapshot(current == null ? 1 : current.revision + 1, palette);
                }
                preferencesRevision = revision;
            }
            return current;
        }
    }

    /** Apply a shipped preset in one preference transaction, preserving unrelated settings. */
    public static void applyPreset(String identifier) {
        if ("system".equals(identifier)) {
            resetToSystem();
            return;
        }
        ThemePalette preset = ThemeCatalog.find(identifier);
        if (preset == null) throw new IllegalArgumentException("Unknown Afterglow theme: " + identifier);
        Map<String, Object> changes = clearedColors();
        for (Map.Entry<ThemeRole, Integer> entry : preset.explicitColors().entrySet()) {
            changes.put(entry.getKey().legacyKey, entry.getValue());
        }
        if (preset.glowEnabled != null) changes.put("theme_glowEnabled", preset.glowEnabled);
        AfterglowPrefs.update(changes);
    }

    /** iOS resetToSystemTheme: remove colors, enabled glow and identity; keep saved themes and typography. */
    public static void resetToSystem() {
        Map<String, Object> changes = clearedColors();
        changes.put("theme_glowEnabled", null);
        AfterglowPrefs.update(changes);
    }

    private static Map<String, Object> clearedColors() {
        Map<String, Object> changes = new HashMap<>();
        for (ThemeRole role : ThemeRole.values()) if (role.legacyKey != null) changes.put(role.legacyKey, null);
        changes.put("theme_semanticTheme", null);
        return changes;
    }
}

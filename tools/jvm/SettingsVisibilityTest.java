package app.afterglow.extension.ui;

import android.content.SharedPreferences;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import app.afterglow.extension.InstalledTweaks;
import app.afterglow.extension.settings.AfterglowPrefs;
import app.afterglow.extension.settings.TweakCatalog;
import app.afterglow.extension.ui.SettingsModel.Page;
import app.afterglow.extension.ui.SettingsModel.Row;
import app.afterglow.extension.ui.SettingsModel.SearchEntry;

/**
 * JVM check of the installed-tweak row rules (SettingsBehavior.shown), run by
 * tools/check_settings_ui.py. Uses the real spec and logic with an in-memory
 * SharedPreferences, in two cases: every tweak with a named patch installed, and only "Shorts layout and buttons" installed.
 */
public final class SettingsVisibilityTest {
    /**
     * The tweak ids the named tweak patches mark (every markTweakInstalled("...") under
     * patches/src), passed in by check_settings_ui.py so new tweak batches need no edit here.
     */
    static String[] currentTweaks = new String[0];

    private static int failures;
    private static int checks;

    public static void main(String[] args) throws Exception {
        MemPrefs prefs = new MemPrefs();
        Field field = AfterglowPrefs.class.getDeclaredField("prefs");
        field.setAccessible(true);
        field.set(null, prefs);

        // ---- case 1: every tweak with a named patch. Expectations are derived from TweakCatalog
        // and the spec, so adding tweak batches or implemented keys needs no edit here.
        currentTweaks = args;
        check("named tweak patches passed in", true, args.length > 0);
        install(args);
        for (boolean advanced : new boolean[] {false, true}) {
            AfterglowPrefs.setBool("advancedMode", advanced);
            String c = "all tweaks, advanced " + (advanced ? "on" : "off") + ": ";
            checkKeyedRows(c);
            checkDerivedRows(c);
            expectRows(c, "page:about.credits", allRows("page:about.credits"));
            expectAbout(c);
            check(c + "root.search shown", true, SettingsBehavior.shown(SettingsBehavior.row("root.search")));
            // Destinations whose features are not ported yet stay hidden.
            expectHidden(c, "root.liteMode", "root.aiHome", "root.abLab", "root.themes", "root.vault", "root.tools", "themes.themeStudio");
            expectSearch(c, advanced, list("Advanced"), list("LiteMode", "Theme Presets"));
        }

        // ---- case 2: only "Shorts layout and buttons"
        install("shorts-layout");
        for (boolean advanced : new boolean[] {false, true}) {
            AfterglowPrefs.setBool("advancedMode", advanced);
            String c = "shorts-layout only, advanced " + (advanced ? "on" : "off") + ": ";
            checkKeyedRows(c);
            // iOS shows Shorts > Layout & Buttons only in advanced mode (Settings.x:3013).
            expectRows(c, "page:root", advanced ? list("root.search", "root.shorts", "root.about") : list("root.search", "root.about"));
            expectRows(c, "page:shorts", advanced ? list("shorts.spacer1", "shorts.layoutButtons") : list());
            List<String> layout = new ArrayList<>();
            for (Row row : SettingsBehavior.page("page:shorts.layoutButtons").rows) {
                if (row.key != null && TweakCatalog.isShown(row.key)) layout.add(row.id);
            }
            layout.addAll(list("shortsLayout.reset.spacer", "shortsLayout.reset.header", "shortsLayout.reset.action"));
            int implemented = 0;
            for (String key : TweakCatalog.tweak("shorts-layout").keys) if (TweakCatalog.SHOWN_KEYS.contains(key)) implemented++;
            check(c + "every implemented Shorts layout key has a row", implemented, layout.size() - 3);
            expectRows(c, "page:shorts.layoutButtons", layout);
            expectAbout(c);
            expectHidden(c, "root.privacyAds", "root.player", "root.feed", "root.comments", "root.interface");
            List<String> shownSearch = advanced ? list("HideShortsLike", "Advanced") : list("Advanced");
            List<String> hiddenSearch = advanced ? list("RemoveAds", "HideShortsLogo", "CommentsHeader") : list("HideShortsLike", "RemoveAds");
            expectSearch(c, advanced, shownSearch, hiddenSearch);
        }

        System.out.println((failures == 0 ? "PASS" : "FAIL") + " settings visibility: " + (checks - failures) + "/" + checks + " checks");
        System.exit(failures == 0 ? 0 : 1);
    }

    /** Every keyed row shows exactly when its key is base, or installed and implemented (and its iOS condition holds). */
    private static void checkKeyedRows(String c) {
        int mismatches = 0;
        for (Page page : SettingsSpec.pages()) {
            for (Row row : page.rows) {
                String key = row.registryKey != null ? row.registryKey : row.key;
                if (key == null || row.action == SettingsModel.Action.RESET_DEFAULTS) continue;
                TweakCatalog.Tweak tweak = TweakCatalog.tweakForKey(key);
                boolean expected = SettingsBehavior.isVisible(row) && (TweakCatalog.BASE_KEYS.contains(key)
                        || (tweak != null && InstalledTweaks.has(tweak.id) && TweakCatalog.SHOWN_KEYS.contains(key)));
                if (SettingsBehavior.shown(row) != expected) {
                    mismatches++;
                    System.out.println("  keyed row " + row.id + " (" + key + "): shown=" + SettingsBehavior.shown(row) + " expected=" + expected);
                }
            }
        }
        check(c + "keyed rows follow base/installed/implemented", 0, mismatches);
    }

    /**
     * Rows without their own key follow what the rules say they follow: section members
     * (headers, spacers, captions, links) their section's keyed rows, Reset to Defaults the keys
     * it resets, and sub-page rows whether their page shows a keyed row.
     */
    private static void checkDerivedRows(String c) {
        int mismatches = 0;
        for (Page page : SettingsSpec.pages()) {
            for (Row row : page.rows) {
                Boolean expected = null;
                if (row.action == SettingsModel.Action.RESET_DEFAULTS) {
                    boolean any = false;
                    for (String key : SettingsBehavior.resetKeys(row)) any |= SettingsBehavior.keyVisible(key);
                    expected = any;
                } else if (SettingsBehavior.isFollower(row) && row.section >= 0) {
                    boolean keyed = false, shown = false;
                    for (Row other : page.rows) {
                        if (other.section != row.section || SettingsBehavior.isFollower(other)) continue;
                        keyed = true;
                        shown |= SettingsBehavior.shown(other);
                    }
                    expected = !keyed || shown;
                } else if (row.key == null && row.registryKey == null && row.targetPage != null
                        && row.kind == SettingsModel.Kind.DESTINATION && !row.id.equals("root.search")) {
                    expected = pageHasShownKeyedRow(SettingsBehavior.page(row.targetPage));
                }
                if (expected == null) continue;
                expected &= SettingsBehavior.isVisible(row);
                if (SettingsBehavior.shown(row) != expected) {
                    mismatches++;
                    System.out.println("  row " + row.id + ": shown=" + SettingsBehavior.shown(row) + " expected=" + expected);
                }
            }
        }
        check(c + "keyless rows follow their section, keys or page", 0, mismatches);
    }

    private static boolean pageHasShownKeyedRow(Page page) {
        if (page == null) return false;
        boolean keyed = false;
        for (Row row : page.rows) {
            if (SettingsBehavior.isFollower(row)) continue;
            keyed = true;
            if (SettingsBehavior.shown(row)) return true;
        }
        return !keyed;
    }

    private static void expectAbout(String c) {
        expectRows(c, "page:about", list("about.advancedMode", "about.clearCache", "about.spacer1", "about.header.managePreferences",
                "about.exportPreferences", "about.importPreferences", "about.resetSettings", "about.spacer2", "about.header.debugLogging",
                "about.debugLogEnabled", "about.debugLogFirehose", "about.header.debugLogCategories", "about.debugLogDownloads",
                "about.debugLogPlayerUI", "about.debugLogPremiumControls", "about.debugLogPiP", "about.debugLogProbes",
                "about.debugHUDEnabled", "about.shareDebugLog", "about.clearDebugLog", "about.reset.spacer", "about.reset.header",
                "about.reset.action", "about.spacer3", "about.credits"));
    }

    private static void expectRows(String c, String pageId, List<String> expected) {
        List<String> shown = new ArrayList<>();
        for (Row row : SettingsBehavior.page(pageId).rows) if (SettingsBehavior.shown(row)) shown.add(row.id);
        check(c + pageId, expected, shown);
    }

    private static void expectHidden(String c, String... rowIds) {
        for (String id : rowIds) check(c + id + " hidden", false, SettingsBehavior.shown(SettingsBehavior.row(id)));
    }

    /** Search entries by title key (setting entries) or English title (page/literal entries). */
    private static void expectSearch(String c, boolean advanced, List<String> shown, List<String> hidden) {
        Map<String, Boolean> visible = new HashMap<>();
        for (SearchEntry entry : SettingsSpec.search()) {
            String name = entry.titleKey != null ? entry.titleKey : entry.title.en;
            visible.merge(name, SettingsBehavior.searchEntryShown(entry, advanced), Boolean::logicalOr);
        }
        for (String name : shown) check(c + "search " + name + " shown", true, visible.get(name));
        for (String name : hidden) check(c + "search " + name + " hidden", false, visible.get(name));
        check(c + "search row matches index", SearchIndex.build(advanced).isEmpty() ? Boolean.FALSE : Boolean.TRUE,
                SettingsBehavior.shown(SettingsBehavior.row("root.search")));
    }

    private static List<String> allRows(String pageId) {
        List<String> ids = new ArrayList<>();
        for (Row row : SettingsBehavior.page(pageId).rows) ids.add(row.id);
        return ids;
    }

    private static void install(String... ids) throws Exception {
        Field field = InstalledTweaks.class.getDeclaredField("INSTALLED");
        field.setAccessible(true);
        ((Set<?>) field.get(null)).clear();
        for (String id : ids) InstalledTweaks.mark(id);
    }

    private static List<String> list(String... values) {
        return new ArrayList<>(Arrays.asList(values));
    }

    private static void check(String name, Object want, Object got) {
        checks++;
        if (want == null ? got != null : !want.equals(got)) {
            failures++;
            System.out.println("FAIL " + name + "\n  want " + want + "\n  got  " + got);
        }
    }

    /** Map-backed SharedPreferences (apply == commit). */
    static final class MemPrefs implements SharedPreferences {
        final Map<String, Object> map = new HashMap<>();
        public Map<String, ?> getAll() { return new HashMap<>(map); }
        public String getString(String k, String d) { Object v = map.get(k); return v instanceof String ? (String) v : d; }
        public Set<String> getStringSet(String k, Set<String> d) { return d; }
        public int getInt(String k, int d) { Object v = map.get(k); return v instanceof Integer ? (Integer) v : d; }
        public long getLong(String k, long d) { Object v = map.get(k); return v instanceof Long ? (Long) v : d; }
        public float getFloat(String k, float d) { Object v = map.get(k); return v instanceof Float ? (Float) v : d; }
        public boolean getBoolean(String k, boolean d) { Object v = map.get(k); return v instanceof Boolean ? (Boolean) v : d; }
        public boolean contains(String k) { return map.containsKey(k); }
        public Editor edit() { return new MemEditor(); }
        public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener l) {}
        public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener l) {}

        final class MemEditor implements Editor {
            final Map<String, Object> put = new HashMap<>();
            final Set<String> remove = new HashSet<>();
            boolean clear;
            public Editor putString(String k, String v) { put.put(k, v); return this; }
            public Editor putStringSet(String k, Set<String> v) { put.put(k, v); return this; }
            public Editor putInt(String k, int v) { put.put(k, v); return this; }
            public Editor putLong(String k, long v) { put.put(k, v); return this; }
            public Editor putFloat(String k, float v) { put.put(k, v); return this; }
            public Editor putBoolean(String k, boolean v) { put.put(k, v); return this; }
            public Editor remove(String k) { remove.add(k); return this; }
            public Editor clear() { clear = true; return this; }
            public boolean commit() { apply(); return true; }
            public void apply() { if (clear) map.clear(); for (String k : remove) map.remove(k); map.putAll(put); }
        }
    }
}

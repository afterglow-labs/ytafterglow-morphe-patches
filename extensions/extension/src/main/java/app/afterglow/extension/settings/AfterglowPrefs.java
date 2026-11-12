package app.afterglow.extension.settings;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import app.afterglow.extension.Afterglow;

/**
 * Afterglow's preference store: same keys, types and defaults as the iOS
 * tweak's user defaults ({@link Registry}). Values live in their own
 * SharedPreferences file so they never mix with YouTube's settings.
 */
public final class AfterglowPrefs {
    public static final String FILE = "ytafterglow_prefs";

    /** Called after any Afterglow value changes, with the key that changed. */
    public interface Listener {
        void onChanged(String key);
    }

    private static final Map<String, Descriptor> BY_KEY = new HashMap<>();
    private static final CopyOnWriteArrayList<Listener> LISTENERS = new CopyOnWriteArrayList<>();
    private static volatile SharedPreferences prefs;

    static {
        for (Descriptor descriptor : Registry.ALL) BY_KEY.put(descriptor.key, descriptor);
    }

    private AfterglowPrefs() {}

    public static Descriptor descriptor(String key) {
        return BY_KEY.get(key);
    }

    private static SharedPreferences prefs() {
        SharedPreferences p = prefs;
        if (p == null) {
            Context context = Afterglow.context();
            if (context == null) return null;
            synchronized (AfterglowPrefs.class) {
                if (prefs == null) prefs = context.getSharedPreferences(FILE, Context.MODE_PRIVATE);
                p = prefs;
            }
        }
        return p;
    }

    public static boolean getBool(String key) {
        SharedPreferences p = prefs();
        boolean fallback = defaultBool(key);
        if (p == null) return fallback;
        try {
            return p.getBoolean(key, fallback);
        } catch (ClassCastException e) {
            return fallback;
        }
    }

    public static int getInt(String key) {
        SharedPreferences p = prefs();
        int fallback = defaultInt(key);
        if (p == null) return fallback;
        try {
            return p.getInt(key, fallback);
        } catch (ClassCastException e) {
            return fallback;
        }
    }

    public static String getString(String key) {
        SharedPreferences p = prefs();
        String fallback = defaultString(key);
        if (p == null) return fallback;
        try {
            return p.getString(key, fallback);
        } catch (ClassCastException e) {
            return fallback;
        }
    }

    public static float getFloat(String key, float fallback) {
        SharedPreferences p = prefs();
        if (p == null) return fallback;
        try {
            return p.getFloat(key, fallback);
        } catch (ClassCastException e) {
            return fallback;
        }
    }

    public static boolean contains(String key) {
        SharedPreferences p = prefs();
        return p != null && p.contains(key);
    }

    public static void setBool(String key, boolean value) {
        SharedPreferences p = prefs();
        if (p == null) return;
        p.edit().putBoolean(key, value).apply();
        notifyChanged(key);
    }

    public static void setInt(String key, int value) {
        SharedPreferences p = prefs();
        if (p == null) return;
        p.edit().putInt(key, value).apply();
        notifyChanged(key);
    }

    public static void setString(String key, String value) {
        SharedPreferences p = prefs();
        if (p == null) return;
        if (value == null) p.edit().remove(key).apply();
        else p.edit().putString(key, value).apply();
        notifyChanged(key);
    }

    public static void setFloat(String key, float value) {
        SharedPreferences p = prefs();
        if (p == null) return;
        p.edit().putFloat(key, value).apply();
        notifyChanged(key);
    }

    public static void remove(String key) {
        SharedPreferences p = prefs();
        if (p == null) return;
        p.edit().remove(key).apply();
        notifyChanged(key);
    }

    /** Restores every Afterglow preference to its default (iOS "Restore default settings"). */
    public static void resetAll() {
        SharedPreferences p = prefs();
        if (p == null) return;
        p.edit().clear().apply();
        notifyChanged(null);
    }

    /**
     * Replaces every stored value in one write (iOS importPreferencesData replaces the
     * whole defaults domain). Values must be Boolean, Integer, Long, Float or String;
     * anything else is skipped. Listeners get a single null-key notification.
     */
    public static void replaceAll(Map<String, ?> values) {
        SharedPreferences p = prefs();
        if (p == null) return;
        SharedPreferences.Editor editor = p.edit().clear();
        for (Map.Entry<String, ?> entry : values.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (key == null) continue;
            if (value instanceof Boolean) editor.putBoolean(key, (Boolean) value);
            else if (value instanceof Integer) editor.putInt(key, (Integer) value);
            else if (value instanceof Long) editor.putLong(key, (Long) value);
            else if (value instanceof Float) editor.putFloat(key, (Float) value);
            else if (value instanceof String) editor.putString(key, (String) value);
        }
        editor.apply();
        notifyChanged(null);
    }

    /** Raw access for import/export; values are Boolean, Integer, Float, Long or String. */
    public static Map<String, ?> snapshot() {
        SharedPreferences p = prefs();
        return p == null ? new HashMap<String, Object>() : p.getAll();
    }

    public static void addListener(Listener listener) {
        LISTENERS.addIfAbsent(listener);
    }

    public static void removeListener(Listener listener) {
        LISTENERS.remove(listener);
    }

    private static void notifyChanged(String key) {
        for (Listener listener : LISTENERS) {
            try {
                listener.onChanged(key);
            } catch (Throwable error) {
                Afterglow.error("AfterglowPrefs listener", error);
            }
        }
    }

    private static boolean defaultBool(String key) {
        Descriptor descriptor = BY_KEY.get(key);
        return descriptor != null && descriptor.defaultValue instanceof Boolean && (Boolean) descriptor.defaultValue;
    }

    private static int defaultInt(String key) {
        Descriptor descriptor = BY_KEY.get(key);
        return descriptor != null && descriptor.defaultValue instanceof Integer ? (Integer) descriptor.defaultValue : 0;
    }

    private static String defaultString(String key) {
        Descriptor descriptor = BY_KEY.get(key);
        return descriptor != null && descriptor.defaultValue instanceof String ? (String) descriptor.defaultValue : null;
    }
}

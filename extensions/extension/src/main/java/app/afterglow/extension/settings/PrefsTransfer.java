package app.afterglow.extension.settings;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Preferences export/import in the iOS file format (YTAGUserDefaults.m:194-242):
 * an XML property list {format: "YTAfterglowPreferences", version: 1,
 * exportedAt: seconds since 1970, preferences: {key: value}}.
 *
 * <p>Values map one-to-one: plist booleans, integers, reals and strings become
 * the same SharedPreferences types; the {@code activeTabs} array round-trips
 * through {@link TabStore}'s encoding. iOS stores theme colors and the scrubber
 * image as NSKeyedArchiver/PNG {@code <data>} blobs. PNG scrubber images round-trip;
 * archived UIColor and date values are skipped. Use a theme file for portable colors.
 */
public final class PrefsTransfer {
    public static final String FORMAT = "YTAfterglowPreferences";
    public static final int VERSION = 1;
    public static final String FILE_NAME = "YTAfterglow-preferences.ytagprefs";

    /** Import failure carrying the iOS error text. */
    public static final class ImportException extends Exception {
        public ImportException(String message) {
            super(message);
        }
    }

    private static final class Unsupported {}

    private PrefsTransfer() {}

    // ---------------------------------------------------------------- export

    /** The current preferences as an iOS-compatible XML plist. */
    public static byte[] exportPlist() {
        Map<String, ?> snapshot = AfterglowPrefs.snapshot();
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<!DOCTYPE plist PUBLIC \"-//Apple//DTD PLIST 1.0//EN\" \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">\n");
        xml.append("<plist version=\"1.0\">\n<dict>\n");
        xml.append("\t<key>exportedAt</key>\n\t<real>")
                .append(String.format(Locale.ROOT, "%.6f", System.currentTimeMillis() / 1000.0)).append("</real>\n");
        xml.append("\t<key>format</key>\n\t<string>").append(FORMAT).append("</string>\n");
        xml.append("\t<key>preferences</key>\n\t<dict>\n");
        for (Map.Entry<String, ?> entry : new TreeMap<String, Object>(snapshot).entrySet()) {
            String value = plistValue(entry.getKey(), entry.getValue(), "\t\t");
            if (value == null) continue;
            xml.append("\t\t<key>").append(escape(entry.getKey())).append("</key>\n").append(value);
        }
        xml.append("\t</dict>\n");
        xml.append("\t<key>version</key>\n\t<integer>").append(VERSION).append("</integer>\n");
        xml.append("</dict>\n</plist>\n");
        return xml.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static String plistValue(String key, Object value, String indent) {
        if (ScrubberImage.KEY.equals(key) && value instanceof String) {
            try { java.util.Base64.getDecoder().decode((String) value); }
            catch (IllegalArgumentException invalid) { return null; }
            return indent + "<data>" + value + "</data>\n";
        }
        if (value instanceof Boolean) return indent + ((Boolean) value ? "<true/>" : "<false/>") + "\n";
        if (value instanceof Integer || value instanceof Long) return indent + "<integer>" + value + "</integer>\n";
        if (value instanceof Float || value instanceof Double) {
            return indent + "<real>" + String.format(Locale.ROOT, "%s", value) + "</real>\n";
        }
        if (value instanceof String && TabStore.ACTIVE_TABS_KEY.equals(key)) {
            List<String> tabs = TabStore.decode(value);
            if (tabs != null) return stringArray(tabs, indent);
        }
        if (value instanceof String) return indent + "<string>" + escape((String) value) + "</string>\n";
        if (value instanceof Collection) {
            List<String> strings = new ArrayList<>();
            for (Object item : new TreeSet<Object>((Collection<?>) value)) strings.add(String.valueOf(item));
            return stringArray(strings, indent);
        }
        return null;
    }

    private static String stringArray(List<String> values, String indent) {
        StringBuilder out = new StringBuilder(indent).append("<array>\n");
        for (String value : values) out.append(indent).append("\t<string>").append(escape(value)).append("</string>\n");
        return out.append(indent).append("</array>\n").toString();
    }

    private static String escape(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    // ---------------------------------------------------------------- import

    /**
     * Replaces every Afterglow preference with the file's (iOS importPreferencesData).
     * Returns the number of values skipped because they have no Android form.
     */
    public static int importPlist(InputStream input) throws ImportException {
        byte[] data;
        try {
            data = readAll(input);
        } catch (IOException error) {
            throw new ImportException(error.getMessage() != null ? error.getMessage() : "Failed to import preferences");
        }
        if (data.length == 0) throw new ImportException("The selected preferences file is empty.");

        Object plist;
        try {
            plist = parse(data);
        } catch (Exception error) {
            plist = null;
        }
        if (!(plist instanceof Map)) throw new ImportException("The selected file is not a preferences plist.");

        Map<?, ?> root = (Map<?, ?>) plist;
        Object preferences = root.get("preferences");
        if (preferences == null && root.get("format") == null) preferences = root;
        if (!(preferences instanceof Map)) {
            throw new ImportException("The selected file does not contain YTAfterglow preferences.");
        }

        Map<String, Object> values = new HashMap<>();
        int skipped = 0;
        for (Map.Entry<?, ?> entry : ((Map<?, ?>) preferences).entrySet()) {
            if (!(entry.getKey() instanceof String)) continue;
            String key = (String) entry.getKey();
            Object converted = convert(key, entry.getValue());
            if (converted == null) skipped++;
            else values.put(key, converted);
        }
        AfterglowPrefs.replaceAll(values);
        TabStore.currentActiveTabs();
        return skipped;
    }

    private static Object convert(String key, Object value) {
        if (value instanceof byte[] && ScrubberImage.KEY.equals(key)) {
            try { return ScrubberImage.normalize((byte[]) value); }
            catch (IOException invalid) { return null; }
        }
        if (value instanceof Boolean || value instanceof String) return value;
        if (value instanceof Long) {
            long number = (Long) value;
            return number >= Integer.MIN_VALUE && number <= Integer.MAX_VALUE ? (Object) (int) number : (Object) number;
        }
        if (value instanceof Double) return ((Double) value).floatValue();
        if (value instanceof List) {
            List<String> strings = new ArrayList<>();
            for (Object item : (List<?>) value) {
                if (!(item instanceof String)) return null;
                strings.add((String) item);
            }
            return TabStore.encode(strings);
        }
        return null;
    }

    private static byte[] readAll(InputStream input) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int read;
        while ((read = input.read(buffer)) != -1) {
            if (out.size() + read > 8 * 1024 * 1024) throw new IOException("Preferences file is too large.");
            out.write(buffer, 0, read);
        }
        return out.toByteArray();
    }

    private static Object parse(byte[] data) throws Exception {
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(new java.io.ByteArrayInputStream(data), "UTF-8");
        int event = parser.getEventType();
        while (event != XmlPullParser.END_DOCUMENT) {
            if (event == XmlPullParser.START_TAG && !"plist".equals(parser.getName())) return readValue(parser);
            event = parser.next();
        }
        return null;
    }

    /** Theme files share the iOS XML property-list value grammar. */
    public static Object parsePropertyList(byte[] data) throws Exception {
        if (data.length > ThemeJson.MAX_LENGTH) throw new IOException("Theme file is too large.");
        return parse(data);
    }

    /** Reads the element at the current START_TAG and leaves the parser on its END_TAG. */
    private static Object readValue(XmlPullParser parser) throws Exception {
        if (parser.getDepth() > 32) throw new IOException("Property list is nested too deeply.");
        String name = parser.getName();
        switch (name) {
            case "dict": {
                Map<String, Object> map = new LinkedHashMap<>();
                String key = null;
                while (true) {
                    int event = parser.next();
                    if (event == XmlPullParser.END_DOCUMENT) throw new IOException("truncated plist");
                    if (event == XmlPullParser.END_TAG && "dict".equals(parser.getName())) return map;
                    if (event != XmlPullParser.START_TAG) continue;
                    if ("key".equals(parser.getName())) {
                        key = parser.nextText();
                        if (map.containsKey(key)) throw new IOException("Duplicate property-list key: " + key);
                    } else {
                        Object value = readValue(parser);
                        if (key != null && !(value instanceof Unsupported)) map.put(key, value);
                        else if (key != null) map.put(key, new Unsupported());
                        key = null;
                    }
                }
            }
            case "array": {
                List<Object> list = new ArrayList<>();
                while (true) {
                    int event = parser.next();
                    if (event == XmlPullParser.END_DOCUMENT) throw new IOException("truncated plist");
                    if (event == XmlPullParser.END_TAG && "array".equals(parser.getName())) return list;
                    if (event == XmlPullParser.START_TAG) list.add(readValue(parser));
                }
            }
            case "string":
                return parser.nextText();
            case "data":
                return java.util.Base64.getMimeDecoder().decode(parser.nextText());
            case "integer":
                return Long.parseLong(parser.nextText().trim());
            case "real":
                return Double.parseDouble(parser.nextText().trim());
            case "true":
                skip(parser);
                return Boolean.TRUE;
            case "false":
                skip(parser);
                return Boolean.FALSE;
            default:
                skip(parser);
                return new Unsupported();
        }
    }

    private static void skip(XmlPullParser parser) throws Exception {
        int depth = 1;
        while (depth > 0) {
            int event = parser.next();
            if (event == XmlPullParser.END_DOCUMENT) throw new IOException("truncated plist");
            if (event == XmlPullParser.START_TAG) depth++;
            else if (event == XmlPullParser.END_TAG) depth--;
        }
    }
}

package android.content;

import android.app.Application;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;

/**
 * Mock Android Context for desktop testing.
 * Provides basic shared preferences and file directory functionality.
 */
public class Context {
    private static final Map<String, Object> prefs = new HashMap<>();
    private static final Application app = new Application();
    private final File dataDir;

    public Context() {
        this(new File(System.getProperty("java.io.tmpdir"), "tvjar_test"));
    }

    public Context(File dataDir) {
        this.dataDir = dataDir;
        if (!dataDir.exists()) dataDir.mkdirs();
    }

    public Application getApplicationContext() {
        return app;
    }

    public String getPackageName() {
        return "com.github.catvod.tvjar";
    }

    public File getCacheDir() {
        File dir = new File(dataDir, "cache");
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }

    public File getFilesDir() {
        File dir = new File(dataDir, "files");
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }

    public SharedPreferences getSharedPreferences(String name, int mode) {
        return new SharedPreferences(name);
    }

    public static class SharedPreferences {
        private final String name;
        private final Map<String, Object> store = new HashMap<>();

        SharedPreferences(String name) {
            this.name = name;
        }

        public String getString(String key, String defValue) {
            Object val = store.get(key);
            return val instanceof String ? (String) val : defValue;
        }

        public int getInt(String key, int defValue) {
            Object val = store.get(key);
            return val instanceof Integer ? (Integer) val : defValue;
        }

        public boolean getBoolean(String key, boolean defValue) {
            Object val = store.get(key);
            return val instanceof Boolean ? (Boolean) val : defValue;
        }

        public Editor edit() {
            return new Editor(this);
        }

        public static class Editor {
            private final SharedPreferences prefs;

            Editor(SharedPreferences prefs) {
                this.prefs = prefs;
            }

            public Editor putString(String key, String value) {
                prefs.store.put(key, value);
                return this;
            }

            public Editor putInt(String key, int value) {
                prefs.store.put(key, value);
                return this;
            }

            public Editor putBoolean(String key, boolean value) {
                prefs.store.put(key, value);
                return this;
            }

            public void apply() {
                // no-op for desktop
            }
        }
    }
}
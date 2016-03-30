package lenacom.filer.config;

import lenacom.filer.message.Errors;

import java.awt.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public enum Configuration {
    INSTANCE;

    public static final String CURRENT_PATH_KEY = "currentPath";
    public static final String LOOK_AND_FEEL_CLASS_NAME_KEY = "lookAndFeelClassName";
    public static final String DEFAULT_CHARSET = "defaultCharset";
    public static final String WRAP_LINES = "wrapLines";
    public static final String BOOKMARKS = "bookmarks";
    public static final String CHARSETS = "charsets";
    public static final String IS_SYMLINK_SUPPORTED = "isSymlinkSupported";
    public static final String SHOW_HIDDEN_FILES = "showHiddenFiles";
    public static final String SHOW_ATTRIBUTES = "showAttributes";
    public static final String EYES_FRIENDLY_MODE = "eyesFriendlyMode";
    public static final String EXTENSION_COLORS = "extensionColors";
    public static final String FONT_FAMILY = "fontFamily";
    public static final String FONT_SIZE = "fontSize";
    public static final String TEXT_FILE_MAX_SIZE = "textFileMaxSize";
    public static final String FIND_LIMITED_SIZE = "findLimitedSize";
    public static final String NEW_FILE_EXTENSIONS = "newFileExtensions";
    public static final String MAXIMIZE_VIEW_EDIT = "maximizeViewEdit";
    public static final String MAXIMIZE_FIND = "maximizeFind";
    public static final String NEW_LINE = "newLine";
    public static final String LANGUAGE = "language";
    public static final String WINDOW_WIDTH = "windowWidth";
    public static final String WINDOW_HEIGHT = "windowHeight";
    public static final String WINDOW_X = "windowX";
    public static final String WINDOW_Y = "windowY";

    private final Preferences prefs;
    private static final String ROOT_KEY = "filer";

    private Configuration() {
        prefs = Preferences.userRoot().node(ROOT_KEY);
    }

    public static String getString(String key) {
        return INSTANCE.prefs.get(key, null);
    }

    public static void setString(String key, String value) {
        INSTANCE.put(key, value);
    }

    public static Path getPath(String key) {
        String value = INSTANCE.prefs.get(key, null);
        return value == null? null : Paths.get(value);
    }

    public static void setPath(String key, Path path) {
        INSTANCE.put(key, path.toAbsolutePath());
    }
    
    public static Integer getInteger(String key) {
        String value = INSTANCE.prefs.get(key, null);
        return value == null? null : Integer.valueOf(value);
    }

    public static void setInteger(String key, Integer value) {
        INSTANCE.put(key, value);
    }

    public static Long getLong(String key) {
        return getLong(key, null);
    }

    public static Long getLong(String key, Long defaultValue) {
        String value = INSTANCE.prefs.get(key, null);
        return value == null? defaultValue : Long.valueOf(value);
    }

    public static void setLong(String key, Long value) {
        INSTANCE.put(key, value);
    }

    public static Boolean getBoolean(String key) {
        return getBoolean(key, null);
    }

    public static Boolean getBoolean(String key, Boolean defaultValue) {
        String value = INSTANCE.prefs.get(key, null);
        return value == null? defaultValue : Boolean.valueOf(value);
    }

    public static void setBoolean(String key, Boolean value) {
        INSTANCE.put(key, value.toString());
    }

    public static void remove(String key) {
        synchronized (INSTANCE.prefs) {
            INSTANCE.prefs.remove(key);
            try {
                INSTANCE.prefs.flush();
            } catch (BackingStoreException e) {
                Errors.showError(e);
            }
        }
    }

    private void put(String key, Object value) {
        synchronized (prefs) {
            if (value == null) {
                remove(key);
            } else {
                prefs.put(key, value.toString());
                try {
                    prefs.flush();
                } catch (BackingStoreException e) {
                    Errors.showError(e);
                }
            }
        }
    }

    public static void clear() {
        synchronized (INSTANCE.prefs) {
            try {
                INSTANCE.prefs.clear();
                INSTANCE.prefs.flush();
            } catch (BackingStoreException e) {
                Errors.showError(e);
            }
        }

    }

}

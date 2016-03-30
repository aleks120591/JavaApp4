package lenacom.filer.config;

import lenacom.filer.message.Errors;

import javax.swing.*;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.List;
import java.util.Properties;

public enum Resources {
    INSTANCE;
    private static final String MNEMONIC_SIGN = "&";
    private final static String ELLIPSIS = "...";
    private Properties messages = new Properties();

    private Resources() {
         load();
    }

    private void load() {
        assert(SwingUtilities.isEventDispatchThread());
        messages = new Properties();
        Language lang = Language.getLanguage();
        String fileName = lang.getCode() + ".properties";

        try (InputStreamReader reader = new InputStreamReader(Resources.class.getResourceAsStream(fileName), "UTF-8")) {
            messages.load(reader);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void reload() {
        INSTANCE.load();
    }

    private static String getValue(String key) {
        if (key == null) return "";
        Object value = INSTANCE.messages.get(key);
        if (value != null) return value.toString();
        return null;
    }

    public static String getMessage(String key) {
        return getMessage(new ResourceKey(key));
    }

    public static String getMessageWithEllipsis(String key) {
        return getMessageWithEllipsis(new ResourceKey(key));
    }

    public static String getMessage(ResourceKey key) {
        String value = getValue(key.getKey());
        return value == null? key.getKey() : MessageFormat.format(value.replace(MNEMONIC_SIGN, ""), key.getParams());
    }

    public static String getMessageWithEllipsis(ResourceKey key) {
        String value = getValue(key.getKey());
        value = value == null? key.getKey() : MessageFormat.format(value.replace(MNEMONIC_SIGN, ""), key.getParams());
        return value + ELLIPSIS;
    }

    public static Character getMnemonic(String key) {
        String value = getValue(key);
        if (value != null) {
            int pos = value.indexOf(MNEMONIC_SIGN);
            if (pos >= 0 && pos < (value.length() - 1)) {
                return value.charAt(pos + 1);
            }
        }
        return null;
    }

    public static Character getMnemonic(ResourceKey key) {
        return getMnemonic(key.getKey());
    }

    public static String getGlobTooltip() throws Exception {
        String fileName = "glob." + Language.getLanguage().getCode() + ".html";
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(Resources.class.getResourceAsStream(fileName), "UTF-8"))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }
    }
}

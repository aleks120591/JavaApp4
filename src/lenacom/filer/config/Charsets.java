package lenacom.filer.config;

import javax.swing.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum Charsets {
    INSTANCE;
    private List<Charset> charsets;

    private Charsets() {
        charsets = new ArrayList<>();

        String data = Configuration.getString(Configuration.CHARSETS);
        if (data != null) {
            String[] rows = data.split("\n");
            for (String row : rows) {
                try {
                    Charset charset = Charset.forName(row);
                    if (charset != null) charsets.add(charset);
                } catch (Exception e) {
                    //do nothing
                }
            }
        }
        if (charsets.size() == 0) {
            charsets.add(Charset.defaultCharset());
        }

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                StringBuilder sb = new StringBuilder();
                for (Charset charset : charsets) {
                    if (sb.length() > 0) sb.append("\n");
                    sb.append(charset.name());
                }
                Configuration.setString(Configuration.CHARSETS, sb.toString());
            }
        });
    }

    public static void addCharsetOnTop(Charset charset) {
        assert(SwingUtilities.isEventDispatchThread());
        //add on the top, keep a unique list
        INSTANCE.charsets.remove(charset);
        INSTANCE.charsets.add(0, charset);
    }

    public static void setCharsets(List<Charset> charsets) {
        assert(SwingUtilities.isEventDispatchThread());
        INSTANCE.charsets.clear();
        INSTANCE.charsets.addAll(charsets);
    }

    public static void deleteCharset(Charset charset) {
        assert(SwingUtilities.isEventDispatchThread());
        INSTANCE.charsets.remove(charset);
    }

    public static List<Charset> getCharsets() {
        Collections.sort(INSTANCE.charsets);
        return Collections.unmodifiableList(INSTANCE.charsets);
    }

    public static Charset getDefaultCharset() {
        Charset charset = null;
        String charsetName = Configuration.getString(Configuration.DEFAULT_CHARSET);
        if (charsetName != null) charset = Charset.forName(charsetName);
        if (charset == null) charset = Charset.defaultCharset();
        return charset;
    }

}

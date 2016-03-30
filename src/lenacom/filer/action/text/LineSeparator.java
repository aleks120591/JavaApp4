package lenacom.filer.action.text;

import lenacom.filer.config.Configuration;

enum LineSeparator {
    LF("\n"),
    CR("\r"),
    CRLF("\r\n");

    private final String value;

    private LineSeparator(String value) {
        this.value = value;
    }

    static LineSeparator getCurrentLineSeparator() {
        String value = Configuration.getString(Configuration.NEW_LINE);
        if (value == null) return getDefaultLineSeparator();
        for (LineSeparator lineSeparator : LineSeparator.values()) {
            if (lineSeparator.value.equals(value)) return lineSeparator;
        }
        return getDefaultLineSeparator();
    }

    static void setCurrentLineSeparator(LineSeparator lineSeparator) {
        Configuration.setString(Configuration.NEW_LINE, lineSeparator.value);
    }

    String replaceLineSeparators(String text) {
        return text.replaceAll("\r\n|\r?\n|\r\n?", value);
    }

    private static LineSeparator getDefaultLineSeparator() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.indexOf("win") >= 0) return CRLF; //windows
        if (os.indexOf("mac") >= 0) return CR; //mac
        return LF;
    }
}

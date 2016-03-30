package lenacom.filer.panel;

import lenacom.filer.config.Resources;

public enum XColumn {
    NAME("col.name"),
    EXTENSION("col.ext"),
    SIZE("col.size"),
    DATE("col.changed"),
    ATTRIBUTES("col.attrs");

    private final String key;
    private String title;

    private XColumn(String key) {
        this.key = key;
        this.title = Resources.getMessage(key);
    }

    String getTitle() {
        return title;
    }

    public String toString() {
        return title;
    }

    public static void reload() {
        for (XColumn column: XColumn.values()) {
            column.title = Resources.getMessage(column.key);
        }
    }
}



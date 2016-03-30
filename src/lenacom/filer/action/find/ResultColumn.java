package lenacom.filer.action.find;

import lenacom.filer.config.Resources;

enum ResultColumn {
    PATH(Resources.getMessage("col.path")),
    NAME(Resources.getMessage("col.name")),
    EXTENSION(Resources.getMessage("col.ext")),
    FOUND_EXTRACTS(Resources.getMessage("col.found.extracts"));

    final private String title;

    private ResultColumn(String title) {
        this.title = title;
    }

    String getTitle() {
        return title;
    }

    public String toString() {
        return title;
    }
}

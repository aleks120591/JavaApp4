package lenacom.filer.path;

import lenacom.filer.config.Resources;

public enum PathSizeUnit {
    B(1, Resources.getMessage("size.unit.b")),
    KB(1024, Resources.getMessage("size.unit.kb")),
    MB(1024 * 1024, Resources.getMessage("size.unit.mb")),
    GB(1024 * 1024 * 1024, Resources.getMessage("size.unit.gb"));
    private long bytes;
    private String text;

    private PathSizeUnit(long bytes, String text) {
        this.bytes = bytes;
        this.text = text;
    }

    public long getBytes() {
        return bytes;
    }

    public String toString() {
        return text;
    }
}

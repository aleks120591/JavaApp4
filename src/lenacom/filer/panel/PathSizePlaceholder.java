package lenacom.filer.panel;

import lenacom.filer.path.PathSize;

class PathSizePlaceholder implements PathSize {
    private static final PathSizePlaceholder DIRECTORY_SIZE_PLACEHOLDER = new PathSizePlaceholder("<DIR>");
    private static final PathSizePlaceholder LINK_SIZE_PLACEHOLDER = new PathSizePlaceholder("<LINK>");
    private String placeholder;

    public static PathSizePlaceholder forDirectory() {
        return DIRECTORY_SIZE_PLACEHOLDER;
    }

    public static PathSizePlaceholder forLink() {
        return LINK_SIZE_PLACEHOLDER;
    }

    private PathSizePlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    @Override
    public String toString() {
        return placeholder;
    }

    @Override
    public long getBytes() {
        return 0;
    }
}

package lenacom.filer.action.find;

import lenacom.filer.path.PathIcons;

import javax.swing.*;
import java.nio.file.Path;

abstract class FoundPath extends ProcessedPath {
    private String parent;
    protected String name;
    protected boolean iconIsSet = false;
    private Icon icon;
    private String tooltip;

    FoundPath(Path path) {
        super(path);
        assert(!path.isAbsolute());
    }

    Icon getIcon() {
        if (!iconIsSet) {
            assert(!SwingUtilities.isEventDispatchThread());
            setIcon(PathIcons.getFileIcon(path));
        }
        return icon;
    }

    protected void setIcon(Icon icon) {
        this.icon = icon;
        iconIsSet = true;
    }

    String getParent() {
        if (parent == null) {
            Path parentPath = path.getParent();
            parent = parentPath == null? "" : parentPath.toString();
        }
        return parent;
    }

    abstract String getName();
    abstract void refresh();

    String getTooltip() {
        if (tooltip == null) tooltip = path.toString();
        return tooltip;
    }
}

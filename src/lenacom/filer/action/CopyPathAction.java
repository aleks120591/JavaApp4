package lenacom.filer.action;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.nio.file.Path;
import java.util.Set;

class CopyPathAction extends XAction implements ContextAction {
    private static final Set<SelectionType> types = SelectionType.getAllExceptParentDirectory();

    @Override
    public Set<SelectionType> getSelectionTypes() {
        return types;
    }

    public CopyPathAction(String key, boolean withEllipsis) {
        super(key, withEllipsis);
    }

    @Override
    public boolean isEnabled() {
        SelectionType type = SelectionType.getSelectionType(getTable());
        return types.contains(type);
    }

    @Override
    public void act() {
        Path[] paths = getTable().getSelectedPaths();
        if (paths.length == 0) {
            paths = new Path[]{getTable().getFocusedRow().getPath()};
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < paths.length; i++) {
            if (i > 0) sb.append("\n");
            sb.append(paths[i]);
        }
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(new StringSelection(sb.toString()), null);
    }
}

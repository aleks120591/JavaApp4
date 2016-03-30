package lenacom.filer.action;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.nio.file.Path;

class CopyDirectoryAction extends XAction {
    public CopyDirectoryAction(String key, boolean withEllipsis) {
        super(key, withEllipsis);
    }

    @Override
    public void act() {
        Path path = getTable().getContext().getDirectory();
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(new StringSelection(path.toString()), null);
    }
}

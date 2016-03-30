package lenacom.filer.action;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

class CopyWorkingDirectoryAction extends XAction {

    public CopyWorkingDirectoryAction(String key, boolean withEllipsis) {
        super(key, withEllipsis);
    }

    @Override
    public void act() {
        String path = getTable().getContext().getDirectory().toAbsolutePath().toString();
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(new StringSelection(path), null);
    }
}

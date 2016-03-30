package lenacom.filer.menu;

import lenacom.filer.config.Bookmarks;
import lenacom.filer.message.Messages;
import lenacom.filer.panel.XPanel;

import java.awt.event.ActionEvent;
import java.nio.file.Path;

class ButtonMenuBookmarkItem extends ButtonMenuPathItem {

    ButtonMenuBookmarkItem(XPanel panel, String name, Path path, String tooltip) {
        super(panel, name, path);
        this.setToolTipText(tooltip);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Path panelCurrentDir = panel.getTable().getContext().getDirectory();
        if (panelCurrentDir.equals(path)) return;
        super.actionPerformed(e);
        panelCurrentDir = panel.getTable().getContext().getDirectory();
        if (!panelCurrentDir.equals(path)) {
           //delete bookmark
            Bookmarks.Bookmark bookmark = Bookmarks.getBookmarkByPath(path);
            if (bookmark != null) {
                Messages.showMessage("err.bookmark.does.not.exist", path.toAbsolutePath());
                Bookmarks.deleteBookmark(bookmark);
            }
        }
    }
}

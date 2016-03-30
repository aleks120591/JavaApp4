package lenacom.filer.action;

import lenacom.filer.component.TextDialog;
import lenacom.filer.config.XIcon;
import lenacom.filer.config.Bookmarks;
import lenacom.filer.config.ResourceKey;
import lenacom.filer.config.Resources;
import lenacom.filer.message.Messages;
import lenacom.filer.panel.XTable;
import lenacom.filer.path.PathUtils;

import java.nio.file.Path;

public class BookmarkAction extends XAction {
    private String nameAdd, nameDelete;
    private Character mnemonicAdd, mnemonicDelete;

    BookmarkAction() {
        String keyAdd = "menu.go.bookmark.add";
        String keyDelete = "menu.go.bookmark.delete";
        nameAdd = Resources.getMessageWithEllipsis(keyAdd);
        nameDelete = Resources.getMessage(keyDelete);
        mnemonicAdd = Resources.getMnemonic(keyAdd);
        mnemonicDelete = Resources.getMnemonic(keyDelete);
        setName(nameAdd);
    }

    @Override
    public void act() {
        Path dir = getTable().getContext().getDirectory();
        Bookmarks.Bookmark bookmark = Bookmarks.getBookmarkByPath(dir);
        if (bookmark != null) {
            Bookmarks.deleteBookmark(bookmark);
            Messages.showMessage("bookmark.msg.bookmark.deleted", dir);
        } else {
            new AddBookmarkDialog(getTable(), dir);
        }
    }

    private final static class AddBookmarkDialog extends TextDialog {
        private Path path;
        private AddBookmarkDialog(XTable xtbl, Path dir) {
            super(xtbl.getWrapper(),
                    new ResourceKey("dlg.add.bookmark.title"),
                    new ResourceKey("dlg.add.bookmark.lbl.name"),
                    new ResourceKey("dlg.add.bookmark.lbl.descr", dir));
            this.setText(PathUtils.getName(dir));
            this.path = dir;
            setVisibleRelativeToParent(1.0, null);
        }

        @Override
        protected boolean onOk() {
            //it's allowed to save bookmarks with the same name
            Bookmarks.addBookmark(getText(), path);
            return true;
        }
    }

    private boolean isBookmarkDirectory() {
        Path dir = getTable().getContext().getDirectory();
        Bookmarks.Bookmark bookmark = Bookmarks.getBookmarkByPath(dir);
        return bookmark != null;
    }

    public void update() {
        if (isBookmarkDirectory()) {
            setIcon(XIcon.DELETE_BOOKMARK.getIcon());
            setName(nameDelete);
        } else {
            setIcon(XIcon.BOOKMARK.getIcon());
            setName(nameAdd);
        }
    }

    public Character getMnemonic() {
        return isBookmarkDirectory()? mnemonicDelete : mnemonicAdd;
    }
}

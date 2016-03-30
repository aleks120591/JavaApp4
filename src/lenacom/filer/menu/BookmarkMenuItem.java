package lenacom.filer.menu;

import lenacom.filer.action.BookmarkAction;
import lenacom.filer.panel.XPanel;

import javax.swing.*;

class BookmarkMenuItem extends JMenuItem {
    private BookmarkAction action;

    BookmarkMenuItem(XPanel panel, BookmarkAction action) {
        super(action.clone(panel));
        this.action = (BookmarkAction) this.getAction();
    }

    void update() {
        action.update();
        this.setIcon(action.getIcon());
        this.setText(action.getName());
        if (action.getMnemonic() != null) this.setMnemonic(action.getMnemonic());
    }
}

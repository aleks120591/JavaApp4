package lenacom.filer.menu;

import lenacom.filer.action.Actions;
import lenacom.filer.action.XAction;
import lenacom.filer.component.ButtonMenu;
import lenacom.filer.config.XIcon;
import lenacom.filer.config.Bookmarks;
import lenacom.filer.panel.XPanel;
import lenacom.filer.path.Roots;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.nio.file.Path;
import java.util.List;

public class GoButtonMenu extends ButtonMenu
        implements Roots.RootsListener, Bookmarks.BookmarksListener {
    private XPanel panel;
    private BookmarkMenuItem miBookmark;

    //not static to update when the language is changed
    private final XAction[][] items = new XAction[][]{
        {
            Actions.getOtherPanelDirAction(),
        },
        {
            Actions.getUpDirAction(),
            Actions.getBackAction(),
            Actions.getForwardAction(),
            Actions.getDirectoryTreeAction(),
            Actions.getDirectoryAsTextAction(),
        },
    };

    static final int[] ROOT_KEY_CODES = new int[]{
        KeyEvent.VK_1, KeyEvent.VK_2, KeyEvent.VK_3,
        KeyEvent.VK_4, KeyEvent.VK_5, KeyEvent.VK_6,
        KeyEvent.VK_7, KeyEvent.VK_8, KeyEvent.VK_9
    };

    public GoButtonMenu(XPanel panel) {
        this.setToolTipText("<html>" + Actions.getGoAction().getName() + "<br>Ctrl-G</html>");
        this.setIcon(Actions.getGoAction().getIcon());
        this.panel = panel;

        Roots.addListener(this);
        Bookmarks.addListener(this);
    }

    @Override
    protected void initMenuItems() {
        for (XAction action: items[0]) {
            menu.add(new ButtonMenuItem(panel, action));
        }
        menu.add(new JSeparator());

        //copy bookmarks
        for (final Bookmarks.Bookmark bookmark: Bookmarks.getBookmarks()) {
            String tooltip = bookmark.getPath().toAbsolutePath().toString();
            ButtonMenuBookmarkItem item = new ButtonMenuBookmarkItem(panel, bookmark.getName(), bookmark.getPath(), tooltip);
            menu.add(item);
        }

        if (miBookmark == null) {
            miBookmark = new BookmarkMenuItem(panel, Actions.getBookmarkAction());
        }

        menu.add(miBookmark);
        menu.add(new JSeparator());

        //copy roots
        menu.add(new ButtonMenuItem(panel, Actions.getRootsAction()));
        int rootKeyCodeIndex = 0;
        for (final Path root: Roots.getRoots()) {
            String name = root.toAbsolutePath().toString();
            ButtonMenuPathItem item = new ButtonMenuPathItem(panel, name, root);
            if (rootKeyCodeIndex < ROOT_KEY_CODES.length) {
                int rootKeyCode = ROOT_KEY_CODES[rootKeyCodeIndex++];
                item.setAccelerator(KeyStroke.getKeyStroke(rootKeyCode, InputEvent.CTRL_DOWN_MASK));
            }
            menu.add(item);
        }

        menu.add(new JSeparator());

        for (XAction action: items[1]) {
            menu.add(new ButtonMenuItem(panel, action));
        }
    }

    @Override
    public void rootsChanged(List<Path> roots) {
        menu.removeAll();
    }

    @Override
    public void bookmarksChanged(List<Bookmarks.Bookmark> bookmarks) {
        menu.removeAll();
    }

    @Override
    public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        Component[] components = menu.getComponents();
        ContextMenu.disableActions(components);

        //select active directories
        Path curPath = panel.getTable().getContext().getDirectory();
        for (Component component: components) {
            if (component instanceof ButtonMenuPathItem) {
                ButtonMenuPathItem item = (ButtonMenuPathItem) component;
                boolean selected = false;
                if (item.getPath().equals(curPath)) {
                    selected = true;
                }
                item.setIcon(selected ? XIcon.CURRENT.getIcon() : null);
            }
        }

        miBookmark.update();
    }
}

package lenacom.filer.menu;

import lenacom.filer.action.Actions;
import lenacom.filer.action.XAction;
import lenacom.filer.panel.XPanel;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

public class HeaderContextMenu {
    private static Map<XPanel, JPopupMenu> menus = new HashMap<>();

    private static final XAction[] HEADER_CONTEXT_ACTIONS = new XAction[]{
        Actions.getCopyDirectoryAction(),
        Actions.getDirectoryAsTextAction()
    };

    private static JPopupMenu createMenu(XPanel xpnl) {
        JPopupMenu menu = new JPopupMenu();
        for (XAction action : HEADER_CONTEXT_ACTIONS) {
            JMenuItem item = new JMenuItem(action.clone(xpnl));
            item.setIcon(action.getIcon());
            menu.add(item);
        }
        return menu;
    }

    public static JPopupMenu getMenu(XPanel xpnl) {
        JPopupMenu menu = menus.get(xpnl);
        if (menu == null) {
            menu = createMenu(xpnl);
            menus.put(xpnl, menu);
        }
        return menu;
    }
}

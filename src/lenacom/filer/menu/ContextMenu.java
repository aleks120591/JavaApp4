package lenacom.filer.menu;

import lenacom.filer.action.Actions;
import lenacom.filer.action.ContextAction;
import lenacom.filer.action.SelectionType;
import lenacom.filer.action.XAction;
import lenacom.filer.panel.DirectoryRow;
import lenacom.filer.panel.FileRow;
import lenacom.filer.panel.PathRow;
import lenacom.filer.panel.XTable;
import lenacom.filer.path.PathUtils;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class ContextMenu {
    private static JPopupMenu menu;

    private static final ContextAction[][] ALL_CONTEXT_ACTIONS = new ContextAction[][]{
        {
            Actions.getOpenAction(),
            Actions.getViewAction(),
            Actions.getEditAction(),
        },
        {
            Actions.getRenameAction(),
            Actions.getCopyAction(),
            Actions.getMoveAction(),
            Actions.getDeleteAction(),
        },
        {
            Actions.getPropertiesAction(),
            Actions.getDirectorySizeAction(),
        },
        {
            Actions.getZipAction(),
            Actions.getUnzipAction(),
        },
        {
            Actions.getSymlinkAction(),
            Actions.getCopyPathAction(),
        }
    };

    static {
        menu = new JPopupMenu();
        for (ContextAction[] group : ALL_CONTEXT_ACTIONS) {
            if (menu.getComponents().length > 0) menu.add(new JSeparator());
            for (ContextAction action : group) {
                JMenuItem item = new JMenuItem((XAction) action);
                item.setIcon(((XAction) action).getIcon());
                menu.add(item);
            }
        }
    }

    public static JPopupMenu getMenu() {
        disableActions(menu.getComponents());
        return menu;
    }

    public static void disableActions(Component[] components) {
        for (Component component: components) {
            if (component instanceof JMenuItem && ((JMenuItem) component).getAction() instanceof XAction) {
                JMenuItem item = (JMenuItem) component;
                XAction action = (XAction) item.getAction();
                item.setEnabled(action.isEnabled());
            }
        }
    }
}
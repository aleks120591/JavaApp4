package lenacom.filer.menu;

import lenacom.filer.action.Actions;
import lenacom.filer.action.XAction;
import lenacom.filer.component.ButtonMenu;
import lenacom.filer.config.XIcon;
import lenacom.filer.panel.XPanel;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import java.awt.*;

public class DoButtonMenu extends ButtonMenu {
    private XPanel panel;

    //not static to update when the language is changed
    private final XAction[][] items = new XAction[][] {
        {
            Actions.getNewDirAction(),
            Actions.getNewFileAction(),
            Actions.getSymlinkAction(),
        },
        {
            Actions.getFindAction(),
            Actions.getSortAction(),
            Actions.getRefreshAction(),
        },
        {
            Actions.getSelectAction(),
            Actions.getSelectAllAction(),
        },
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
            Actions.getCompareDirectoriesAction(),
            Actions.getCopyPathAction(),
            Actions.getCopyDirectoryAction(),
        }
    };

    public DoButtonMenu(XPanel panel) {
        this.setToolTipText("<html>" + Actions.getDoAction().getName() + "<br>Ctrl-D</html>");
        this.setIcon(Actions.getDoAction().getIcon());
        this.panel = panel;
    }

    @Override
    protected void initMenuItems() {
        for (int i = 0; i < items.length; i++) {
            XAction[] group = items[i];
            if (i > 0) menu.add(new JSeparator());
            for (XAction action: group) {
                menu.add(new ButtonMenuItem(panel, action));
            }
        }
    }

    @Override
    public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        if (menu.getComponentCount() == 0) {
            initMenuItems();
        }
        ContextMenu.disableActions(menu.getComponents());
    }
}

package lenacom.filer.action.find;

import lenacom.filer.config.Resources;
import lenacom.filer.config.XIcon;
import lenacom.filer.path.PathUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class ResultContextMenu implements ActionListener {
    private ResultTable tblResult;
    private JPopupMenu menu;
    private JMenuItem miGoTo, miOpen, miView, miEdit, miCopyPath;

    ResultContextMenu(ResultTable tblResult) {
        this.tblResult = tblResult;

        miGoTo = createMenuItem("dlg.find.menu.goto");
        miOpen = createMenuItem("dlg.find.menu.open");
        miView = createMenuItem("dlg.find.menu.view");
        miEdit = createMenuItem("dlg.find.menu.edit");
        miEdit.setIcon(XIcon.EDIT.getIcon());
        miCopyPath = createMenuItem("dlg.find.menu.copy.path");

        menu = new JPopupMenu();
        menu.add(miGoTo);
        menu.add(new JSeparator());
        if (PathUtils.isOpenSupported()) menu.add(miOpen);
        menu.add(miView);
        menu.add(miEdit);
        menu.add(new JSeparator());
        menu.add(miCopyPath);
    }

    JPopupMenu getFileMenu() {
        assert(SwingUtilities.isEventDispatchThread());
        for (int i = 0, n = menu.getComponentCount(); i < n; i++) {
            menu.getComponent(i).setVisible(true);
        }
        return menu;
    }

    JPopupMenu getDirectoryMenu() {
        assert(SwingUtilities.isEventDispatchThread());
        assert(SwingUtilities.isEventDispatchThread());
        for (int i = 0, n = menu.getComponentCount(); i < n; i++) {
            Component c = menu.getComponent(i);
            c.setVisible(c == miGoTo);
        }
        return menu;
    }

     private JMenuItem createMenuItem(String key) {
         JMenuItem mi = new JMenuItem(Resources.getMessage(key));
         mi.addActionListener(this);
         return mi;
     }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == miGoTo) {
            tblResult.goTo();
        } else if (source == miOpen) {
            tblResult.open();
        } else if (source == miView) {
            tblResult.view();
        } else if (source == miEdit) {
            tblResult.edit();
        } else if (source == miCopyPath) {
            tblResult.copyPath();
        }
    }
}

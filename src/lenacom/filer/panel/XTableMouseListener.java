package lenacom.filer.panel;

import lenacom.filer.action.Actions;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

class XTableMouseListener extends MouseAdapter {
    private XTableImpl xTable;

    XTableMouseListener(XTableImpl xTable) {
        this.xTable = xTable;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        processMouseEvent(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        processMouseEvent(e);
    }

    private void processMouseEvent(MouseEvent e) {
        //popup trigger depends on look and feel
        if (e.isPopupTrigger()) {
            xTable.showContextMenu(e.getX(), e.getY());
        }
        XPanels.setActiveTable(xTable);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
            int rowIndex = xTable.getTable().rowAtPoint(e.getPoint());
            if (rowIndex >= 0) {
                PathRow row = xTable.getRow(rowIndex);
                xTable.setSelectedPaths(row.getPath());
                Actions.getOpenAction().act();
            }
        } else {
            XPanels.setActiveTable(xTable);
        }
    }
}

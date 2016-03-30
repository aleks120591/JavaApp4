package lenacom.filer.action.find;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

class ResultTableMouseListener extends MouseAdapter {
    private ResultTable tblResult;

    ResultTableMouseListener(ResultTable tblResult) {
        this.tblResult = tblResult;
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
            tblResult.showContextMenu(e.getX(), e.getY());
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
            int rowIndex = tblResult.getTable().rowAtPoint(e.getPoint());
            if (rowIndex >= 0) {
                tblResult.goTo();
            }
        }
    }

}

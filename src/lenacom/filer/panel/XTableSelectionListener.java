package lenacom.filer.panel;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.util.ArrayList;
import java.util.List;

class XTableSelectionListener implements ListSelectionListener {
    private XTableImpl xTable;
    private List<XTableListener> listeners = new ArrayList<>();

    XTableSelectionListener(XTableImpl xTable) {
        this.xTable = xTable;
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) return;
        int[] rowIndexes = xTable.getTable().getSelectedRows();
        //unselect parent dir if something else is selected
        if (rowIndexes.length > 1) {
            PathRow parentRow = xTable.getParentRow();
            ListSelectionModel selModel = xTable.getTable().getSelectionModel();
            for (int rowIndex : rowIndexes) {
                PathRow row = xTable.getRow(rowIndex);
                if (row == parentRow) {
                    if (rowIndex == xTable.getFocusedRowIndex()) {
                        //select only parent row
                        selModel.setSelectionInterval(rowIndex, rowIndex);
                    } else {
                        //unselect parent row
                        final int oldFocusedRowIndex = xTable.getFocusedRowIndex();
                        selModel.removeSelectionInterval(rowIndex, rowIndex);
                        selModel.addSelectionInterval(oldFocusedRowIndex, oldFocusedRowIndex);
                        return; //don't notify listeners
                    }
                }
            }
        }
        notifyListeners();
    }

    private void notifyListeners() {
        int i = 0;
        PathRow[] rows = xTable.getSelectedRows();
        while (i < listeners.size()) {
            listeners.get(i++).selectionChanged(rows);
        }
    }

    void addListener(XTableListener l) {
        listeners.add(l);
    }

     void removeListener(XTableListener l) {
        listeners.remove(l);
    }
}

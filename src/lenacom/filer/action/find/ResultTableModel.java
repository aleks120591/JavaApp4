package lenacom.filer.action.find;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class ResultTableModel extends AbstractTableModel {
    private List<ResultTableModelCell[]> cells = new ArrayList<>();
    private List<FoundPath> foundPaths = new ArrayList<>();

    void clear() {
        if (foundPaths.size() == 0) return;
        int toIndex = foundPaths.size() - 1;
        foundPaths.clear();
        cells.clear();
        this.fireTableRowsDeleted(0, toIndex);
    }

    void addAll(List<FoundPath> foundPaths) {
        assert(SwingUtilities.isEventDispatchThread());
        if (foundPaths.size() == 0) return;
        for (FoundPath foundPath: foundPaths) {
            ResultTableModelCell[] row = new ResultTableModelCell[ResultColumn.values().length];
            int i = 0;
            for (ResultColumn column: ResultColumn.values()) {
                row[i++] = new ResultTableModelCell(foundPath, column);
            }
            cells.add(row);
        }

        int fromIndex = this.foundPaths.size();
        this.foundPaths.addAll(foundPaths);
        int toIndex = this.foundPaths.size() - 1;
        this.fireTableRowsInserted(fromIndex, toIndex);
    }

    List<FoundPath> getPathsCopy() {
        List<FoundPath> copy = new ArrayList<>(foundPaths.size());
        copy.addAll(foundPaths);
        return Collections.unmodifiableList(copy);
    }

    @Override
    public int getRowCount() {
        return foundPaths.size();
    }

    @Override
    public int getColumnCount() {
        return ResultColumn.values().length + 1;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return cells.get(rowIndex)[columnIndex];
    }

    FoundPath getPathAt(int rowIndex) {
        return foundPaths.get(rowIndex);
    }

    @Override
    public Class<? extends Object> getColumnClass(int c) {
        return ResultTableModelCell.class;
    }

    void refresh() {
        List<FoundPath> foundPaths = getPathsCopy();
        for(FoundPath path: foundPaths) path.refresh();
        clear();
        addAll(foundPaths);
    }
}
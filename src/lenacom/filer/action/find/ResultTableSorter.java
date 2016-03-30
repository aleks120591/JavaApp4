package lenacom.filer.action.find;

import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

class ResultTableSorter extends TableRowSorter<TableModel> {

    ResultTableSorter(ResultTableModel model) {
        super(model);
         setSortsOnUpdates(true);
        setMaxSortKeys(1);
    }

    @Override
    public void rowsInserted(int firstRow, int endRow) {
        super.rowsInserted(firstRow, endRow);
        sort();
    }

    @Override
    public void rowsUpdated(int firstRow, int endRow) {
        super.rowsUpdated(firstRow, endRow);
        sort();
    }

    @Override
    public void rowsUpdated(int firstRow, int endRow, int column) {
        super.rowsUpdated(firstRow, endRow, column);
        sort();
    }
}

package lenacom.filer.panel;

import javax.swing.*;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

class XTableSorter extends TableRowSorter<TableModel> {

    XTableSorter(XTableModel model) {
        super(model);

        setSortsOnUpdates(true);
        setMaxSortKeys(1);

        XTableModelCellComparator comparator = new XTableModelCellComparator();
        for (XColumn column : XColumn.values()) {
            setComparator(column.ordinal(), comparator);
        }

        sortByColumn(XColumn.NAME);
    }

    private int getSortColumnIndex() {
        if (getSortKeys().size() == 0) return -1;
        return getSortKeys().get(0).getColumn();
    }

    void sortByColumn(XColumn column) {
        int sortColIndex = getSortColumnIndex();
        if (sortColIndex == column.ordinal()) {
            toggleSortOrder(sortColIndex);
        } else {
            sortColIndex = column.ordinal();
            List<SortKey> sortKeys = new ArrayList<>(1);
            sortKeys.add(new RowSorter.SortKey(sortColIndex, SortOrder.ASCENDING));
            setSortKeys(sortKeys);
            sort();
        }
    }

    XColumn getSortColumn() {
        int sortColIndex = getSortColumnIndex();
        return sortColIndex == -1? null : XColumn.values()[sortColIndex];
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

    private final class XTableModelCellComparator<T> implements Comparator<XTableModelCell<T>> {
        @Override
        public int compare(XTableModelCell<T> c1, XTableModelCell<T> c2) {
            boolean asc = XTableSorter.this.getSortKeys().get(0).getSortOrder() == SortOrder.ASCENDING;

            boolean isDir1 = c1.getRow() instanceof DirectoryRow;
            boolean isDir2 = c2.getRow() instanceof DirectoryRow;
            if (isDir1 && !isDir2 || (c1.getRow() instanceof ParentDirectoryRow)) {
                return asc? -1 : 1;
            }

            if (isDir2 && !isDir1 || (c2.getRow() instanceof ParentDirectoryRow)) {
                return asc? 1 : -1;
            }
            return c1.compareTo(c2);
        }
    }
}

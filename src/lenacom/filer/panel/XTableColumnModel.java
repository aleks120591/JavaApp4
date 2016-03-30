package lenacom.filer.panel;

import lenacom.filer.config.Settings;

import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import java.util.EnumMap;

class XTableColumnModel extends DefaultTableColumnModel {
    private EnumMap<XColumn, TableColumn> tableColumns = new EnumMap<>(XColumn.class);
    private XColumn[] columns;

    XTableColumnModel() {
        int i = 0;
        columns = Settings.isShowAttributes()?
            new XColumn[]{XColumn.NAME, XColumn.EXTENSION, XColumn.SIZE, XColumn.DATE, XColumn.ATTRIBUTES} :
            new XColumn[]{XColumn.NAME, XColumn.EXTENSION, XColumn.SIZE, XColumn.DATE};
        for (XColumn column: columns) {
            TableColumn tableColumn = new TableColumn();
            tableColumn.setHeaderValue(column.getTitle());
            tableColumn.setModelIndex(i++);
            addColumn(tableColumn);
            tableColumns.put(column, tableColumn);
        }
    }

    TableColumn getColumn(XColumn column) {
        return tableColumns.get(column);
    }

    XColumn[] getXColumns() {
        return columns;
    }
}

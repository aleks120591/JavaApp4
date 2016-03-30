package lenacom.filer.action.find;

import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import java.util.EnumMap;

class ResultTableColumnModel extends DefaultTableColumnModel {
    private EnumMap<ResultColumn, TableColumn> columns = new EnumMap<>(ResultColumn.class);
    ResultTableColumnModel() {
        int i = 0;
        for (ResultColumn column: ResultColumn.values()) {
            TableColumn tableColumn = new TableColumn();
            tableColumn.setHeaderValue(column.getTitle());
            tableColumn.setModelIndex(i++);
            addColumn(tableColumn);
            columns.put(column, tableColumn);
        }
    }

    TableColumn getColumn(ResultColumn column) {
        return columns.get(column);
    }

}

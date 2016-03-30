package lenacom.filer.panel;

import javax.swing.*;
import javax.swing.table.TableColumn;
import java.util.EnumMap;

class XTableColumnAdjuster {
    private XTableImpl xTable;
    private EnumMap<XColumn, Integer> widths = new EnumMap<>(XColumn.class);
    private final static int GAP = 5;
    private XTableCellRenderer renderer;

    XTableColumnAdjuster(XTableImpl xtbl) {
        this.xTable = xtbl;
    }

    void adjust() {
        if (renderer == null) {
            renderer = (XTableCellRenderer) xTable.getTable().getDefaultRenderer(XTableModelCell.class);
        }
        XTableColumnModel columnModel = xTable.getColumnModel();

        int tableWidth = ((JScrollPane) xTable.getWrapper()).getViewport().getWidth(); //insets are 0
        if (tableWidth == 0) return; //not showing yet
        widths.clear();
        int columnWidthMaxLimit = tableWidth / 4;
        for (int i = 0, n = xTable.getRowCount(); i < n; i++) {
            PathRow row = xTable.getRow(i);
            for (XColumn column: xTable.getColumns()) {
                if (column == XColumn.NAME) continue;
                String cellValue = XValue.getValue(row, column);
                int width = renderer.getMetrics().stringWidth(cellValue);
                width = Math.min(width, columnWidthMaxLimit);
                Integer maxWidth = widths.get(column);
                if (maxWidth == null || width > maxWidth) widths.put(column, width);
            }
        }

        JTable table = xTable.getTable();

        //calculate width of name column
        int nameColumnWidth = tableWidth;
        for (XColumn column: xTable.getColumns()) {
            if (column == XColumn.NAME) continue;
            Integer maxWidth = widths.get(column);
            if (maxWidth == null) {
                maxWidth = columnWidthMaxLimit;
                widths.put(column, maxWidth);
            }
            maxWidth += GAP;
            widths.put(column, maxWidth);
            nameColumnWidth -= maxWidth;
        }

        int nameColumnWidthMinLimit = tableWidth / 6;
        nameColumnWidth = Math.max(nameColumnWidth, nameColumnWidthMinLimit);
        widths.put(XColumn.NAME, nameColumnWidth);

        int sumColumnWidths = 0;
        for (XColumn column: xTable.getColumns()) {
            TableColumn tableColumn = columnModel.getColumn(column);
            Integer maxWidth = widths.get(column);
            if (maxWidth != null) {
                table.getTableHeader().setResizingColumn(tableColumn);
                tableColumn.setWidth(maxWidth);
                //if some column is empty it gets 0 width but in reality the column header is not 0
                //so we check the width again and will shorten the name column if needed
                sumColumnWidths += tableColumn.getWidth();
            }
        }

        if (tableWidth < sumColumnWidths) {
            nameColumnWidth -= (sumColumnWidths - tableWidth);
            if (nameColumnWidth > nameColumnWidthMinLimit) {
                TableColumn tableColumn = columnModel.getColumn(XColumn.NAME);
                table.getTableHeader().setResizingColumn(tableColumn);
                tableColumn.setWidth(nameColumnWidth);
            }
        }
    }
}


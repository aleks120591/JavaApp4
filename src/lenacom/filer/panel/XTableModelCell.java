package lenacom.filer.panel;

import java.util.Date;

abstract class XTableModelCell<T> implements Comparable<XTableModelCell<T>> {
    private XColumn column;
    private PathRow row;
    protected T sortValue;

    private static class SizeCell extends XTableModelCell<Long> {
        private SizeCell(PathRow row) {
            super(row, XColumn.SIZE, row.getSize().getBytes());
        }

        @Override
        public int compareTo(XTableModelCell<Long> cell) {
            //first sort - desc
            return SizeCell.this.sortValue.compareTo(cell.sortValue);
        }
    }

    private static class DateCell extends XTableModelCell<Date> {
        private DateCell(PathRow row) {
            super(row, XColumn.DATE, row.getDate());
        }

        @Override
        public int compareTo(XTableModelCell<Date> cell) {
            return cell.sortValue.compareTo(DateCell.this.sortValue);
        }
    }

    private static class StringCell extends XTableModelCell<String> {
        private StringCell(PathRow row, XColumn column, String value) {
            super(row, column, value.toLowerCase());
        }

        @Override
        public int compareTo(XTableModelCell<String> cell) {
            return StringCell.this.sortValue.compareTo(cell.sortValue);
        }
    }

    static XTableModelCell create(PathRow row, XColumn column) {
        switch (column) {
            case NAME:
                return new StringCell(row, column, row.getName());
            case EXTENSION:
                return new StringCell(row, column, row.getExtension());
            case SIZE:
                return new SizeCell(row);
            case DATE:
                return new DateCell(row);
            case ATTRIBUTES:
                return new StringCell(row, column, row.getAttributes());
        }
        throw new RuntimeException("Unexpected column " + column + ".");
    }

    private XTableModelCell(PathRow row, XColumn column, T sortValue) {
        this.row = row;
        this.column = column;
        this.sortValue = sortValue;
    }

    PathRow getRow() {
        return row;
    }

    XColumn getColumn() {
        return column;
    }
}

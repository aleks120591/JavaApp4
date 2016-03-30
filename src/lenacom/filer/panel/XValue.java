package lenacom.filer.panel;

class XValue {
    static String getValue(PathRow row, XColumn column) {
        switch (column) {
            case NAME: return row.getName();
            case EXTENSION: return row.getExtension();
            case SIZE: return row.getSize().toString();
            case DATE: return row.getFormattedDate();
            case ATTRIBUTES: return row.getAttributes();
        }
        return "";
    }
}

package lenacom.filer.action.find;

class ResultTableModelCell implements Comparable<ResultTableModelCell>{
    private ResultColumn column;
    private FoundPath foundPath;
    private Object sortValue;

    ResultTableModelCell(FoundPath foundPath, ResultColumn column) {
        this.foundPath = foundPath;
        this.column = column;
        switch (column) {
            case PATH:
                sortValue = foundPath.getPath().toString();
                break;
            case NAME:
                sortValue = foundPath.getName();
                break;
            case EXTENSION:
                sortValue = foundPath instanceof FoundFile?
                        ((FoundFile) foundPath).getExtension() : "";
                break;
            case FOUND_EXTRACTS:
                sortValue = foundPath instanceof FoundFile?
                        ((FoundFile) foundPath).getCountExtracts() : 0;
                break;
        }
    }

    ResultColumn getColumn() {
        return column;
    }

    FoundPath getFoundPath() {
        return foundPath;
    }

    @Override
    public int compareTo(ResultTableModelCell c) {
        if (this.sortValue instanceof String) {
            return ((String) this.sortValue).compareTo((String) c.sortValue);
        } else {
            return ((Integer) this.sortValue).compareTo((Integer) c.sortValue);
        }
    }
}

package lenacom.filer.action;

import lenacom.filer.panel.DirectoryRow;
import lenacom.filer.panel.FileRow;
import lenacom.filer.panel.PathRow;
import lenacom.filer.panel.XTable;

import java.util.HashSet;
import java.util.Set;

public enum SelectionType {
    PARENT_DIRECTORY, FILE, ZIP, DIRECTORY, SYMLINK_FILE, SYMLINK_DIRECTORY, MULTIPLE;

    public static SelectionType getSelectionType(XTable table) {
        PathRow[] rows = table.getSelectedRows();
        if (rows.length == 0) {
            rows = new PathRow[]{ table.getFocusedRow() };
        }
        if (rows.length == 1) {
            PathRow row = rows[0];
            if (row == table.getParentRow()) {
                return PARENT_DIRECTORY;
            } else if (row.isSymlink()) {
                return row instanceof FileRow ? SYMLINK_FILE : SYMLINK_DIRECTORY;
            } else {
                if (row instanceof DirectoryRow) {
                    return  DIRECTORY;
                } else {
                    return ((FileRow) row).isZip()? ZIP : FILE;
                }
            }
        } else {
            return MULTIPLE;
        }
    }

    private static Set<SelectionType> getAll() {
        Set<SelectionType> types = new HashSet<>();
        for (SelectionType type: SelectionType.values()) types.add(type);
        return types;
    }

    static Set<SelectionType> getAllExceptMultiple() {
        Set<SelectionType> types = getAll();
        types.remove(SelectionType.MULTIPLE);
        return types;
    }

    static Set<SelectionType> getAllExceptParentDirectory() {
        Set<SelectionType> types = getAll();
        types.remove(SelectionType.PARENT_DIRECTORY);
        return types;
    }

    static Set<SelectionType> getAllExceptMultipleAndParentDirectory() {
        Set<SelectionType> types = getAllExceptMultiple();
        types.remove(SelectionType.PARENT_DIRECTORY);
        return types;
    }
}

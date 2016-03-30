package lenacom.filer.action;

import lenacom.filer.panel.DirectoryRow;
import lenacom.filer.panel.PathRow;
import lenacom.filer.panel.XTableContext;
import lenacom.filer.path.DirectorySizeCalculator;

import java.util.HashSet;
import java.util.Set;

class DirectorySizeAction extends XAction implements ContextAction {
    private static final Set<SelectionType> types;

    static {
        types = new HashSet<>();
        types.add(SelectionType.MULTIPLE);
        types.add(SelectionType.DIRECTORY);
    }

    @Override
    public Set<SelectionType> getSelectionTypes() {
        return types;
    }

    public DirectorySizeAction(String key, boolean withEllipsis) {
        super(key, withEllipsis);
    }

    @Override
    public boolean isEnabled() {
        SelectionType type = SelectionType.getSelectionType(getTable());
        if (!types.contains(type)) return false;
        PathRow[] rows = getTable().getSelectedRows();
        for (PathRow row: rows) {
            if (row instanceof DirectoryRow && ((DirectoryRow) row).canCalculateSize()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void act() {
        if (!isEnabled()) return;
        PathRow[] rows = getTable().getSelectedRows();
        XTableContext context = getTable().getContext();
        for (PathRow row: rows) {
            if (row instanceof DirectoryRow) {
                DirectoryRow dirRow = (DirectoryRow) row;
                if (dirRow.canCalculateSize()) {
                    if (context.isZip()) {
                        DirectorySizeCalculator.calculate(context.getZipWorker(), dirRow);
                    } else {
                        DirectorySizeCalculator.calculate(dirRow);
                    }
                }
            }
        }
    }

}

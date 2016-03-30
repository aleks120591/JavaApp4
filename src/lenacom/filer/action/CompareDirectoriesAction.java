package lenacom.filer.action;

import lenacom.filer.config.ResourceKey;
import lenacom.filer.panel.PathRow;
import lenacom.filer.panel.XPanels;
import lenacom.filer.panel.XTable;
import lenacom.filer.path.PathUtils;
import lenacom.filer.root.RootFrame;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class CompareDirectoriesAction extends XAction {

    public CompareDirectoriesAction(String key, boolean withEllipsis) {
        super(key, withEllipsis);
    }

    @Override
    public void act() {
        XTable leftTable = XPanels.getLeftPanel().getTable();
        XTable rightTable = XPanels.getRightPanel().getTable();

        Map<String, PathRow> leftRows= getAllRows(leftTable);
        Map<String, PathRow> rightRows = getAllRows(rightTable);

        final Set<Path> leftDiff = new HashSet<>();
        final Set<Path> leftSameNameDifferentSize = new HashSet<>();
        final Set<Path> rightSameNameDifferentSize = new HashSet<>();
        for (String name: leftRows.keySet()) {
            PathRow leftRow = leftRows.get(name);
            if (rightRows.containsKey(name)) {
                PathRow rightRow = rightRows.get(name);
                Long leftSize = leftRow.getSize().getBytes();
                Long rightSize = rightRow.getSize().getBytes();
                boolean equalSize =
                    (leftSize == null && rightSize == null) ||
                    (leftSize != null && rightSize != null && leftSize.equals(rightSize));
                if (!equalSize) {
                    leftSameNameDifferentSize.add(leftRow.getPath());
                    rightSameNameDifferentSize.add(rightRow.getPath());
                }
                rightRows.remove(name);
            } else {
                leftDiff.add(leftRow.getPath());
            }
        }
        final Set<Path> rightDiff = new HashSet<>();
        for (PathRow row: rightRows.values()) {
            rightDiff.add(row.getPath());
        }

        if (leftSameNameDifferentSize.size() > 0) {
            ConfirmPathsDialog dlg = new ConfirmPathsDialog(
                RootFrame.getRoot(),
                new ResourceKey("dlg.compare.dirs.title"),
                new ResourceKey("dlg.compare.dirs.msg.select.different.size"),
                toArray(leftSameNameDifferentSize)) {
                @Override
                protected boolean onOk() {
                    leftDiff.addAll(leftSameNameDifferentSize);
                    rightDiff.addAll(rightSameNameDifferentSize);
                    return true;
                }
            };
            dlg.setVisibleRelativeToParent();
        }
        leftTable.setSelectedPaths(toArray(leftDiff));
        rightTable.setSelectedPaths(toArray(rightDiff));
    }

    private Map<String, PathRow> getAllRows(XTable table) {
        PathRow parentRow = table.getParentRow();
        Map<String, PathRow> paths = new HashMap<>();
        PathRow[] rows = table.getAllRows();
        for (PathRow row : rows) {
            if (row == parentRow) continue;
            paths.put(PathUtils.getName(row.getPath()), row);
        }
        return paths;
    }

    private Path[] toArray(Set<Path> set) {
        return set.toArray(new Path[set.size()]);
    }
}

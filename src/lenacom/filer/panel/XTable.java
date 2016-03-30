package lenacom.filer.panel;

import lenacom.filer.component.Refreshable;

import javax.swing.*;
import java.nio.file.Path;

public interface XTable extends Refreshable {
    JComponent getWrapper();

    XTableContext getContext();

    XColumn[] getColumns();

    void setContextDirectory(Path path);

    void showContextMenu();

    PathStack getStack();

    PathRow[] getAllRows();

    PathRow getFocusedRow();

    ParentDirectoryRow getParentRow();

    PathRow[] getSelectedRows();

    Path[] getSelectedPaths();
    
    void setSelectedPaths(Path... paths);

    void selectAll();

    void addListener(XTableListener l);

    void removeListener(XTableListener l);

    void setSortColumn(XColumn col);

    XColumn getSortColumn();

    void goTo(Path path);
}

package lenacom.filer.action;

import lenacom.filer.config.XIcon;
import lenacom.filer.config.ResourceKey;
import lenacom.filer.message.Errors;
import lenacom.filer.panel.ParentDirectoryRow;
import lenacom.filer.panel.XTableContext;
import lenacom.filer.root.FileOperationWorkers;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

class DeleteAction extends XAction implements ContextAction {
    private static final Set<SelectionType> types = SelectionType.getAllExceptParentDirectory();

    @Override
    public Set<SelectionType> getSelectionTypes() {
        return types;
    }

    public DeleteAction(String key, boolean withEllipsis) {
        super(key, withEllipsis);
        this.setIcon(XIcon.DELETE.getIcon());
    }

    @Override
    public boolean isEnabled() {
        SelectionType type = SelectionType.getSelectionType(getTable());
        return types.contains(type);
    }

    @Override
    public void act() {
        final Path[] paths = getTable().getSelectedPaths();
        if (paths.length == 0) return;
        ConfirmPathsDialog dlg = new ConfirmPathsDialog(getTable().getWrapper(),
                new ResourceKey("confirm.title"),
                new ResourceKey("confirm.delete.files"), paths) {
            @Override
            protected boolean onOk() {
                XTableContext context = getTable().getContext();
                if (!context.isZip()) {
                    new DeleteWorker(paths).execute();
                    ParentDirectoryRow parentRow = getTable().getParentRow();
                    if (parentRow != null) {
                        Path pathToSelect = parentRow.getPath();
                        getTable().setSelectedPaths(pathToSelect);
                    }
                } else {
                    try {
                        context.getZipWorker().delete(paths);
                    } catch (IOException e) {
                        Errors.showError(this, e);
                        getTable().refresh();
                    }
                }
                return true;
            }
        };
        dlg.setVisibleRelativeToParent();
    }
}

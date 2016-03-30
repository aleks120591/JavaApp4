package lenacom.filer.action;

import lenacom.filer.config.XIcon;
import lenacom.filer.config.ResourceKey;
import lenacom.filer.message.Confirmation;
import lenacom.filer.message.Errors;
import lenacom.filer.panel.XPanels;
import lenacom.filer.panel.XTable;
import lenacom.filer.panel.XTableAdapter;
import lenacom.filer.panel.XTableContext;
import lenacom.filer.path.processor.RenameDialog;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Set;

class RenameAction extends XAction implements ContextAction {
    private static final Set<SelectionType> types = SelectionType.getAllExceptMultipleAndParentDirectory();

    @Override
    public Set<SelectionType> getSelectionTypes() {
        return types;
    }

    public RenameAction(String key, boolean withEllipsis) {
        super(key, withEllipsis);
        this.setIcon(XIcon.RENAME.getIcon());
    }

    @Override
    public boolean isEnabled() {
        SelectionType type = SelectionType.getSelectionType(getTable());
        return types.contains(type);
    }

    @Override
    public void act() {
        final XTableContext context = getTable().getContext();
        Path path = getTable().getFocusedRow().getPath();

        if (context.getPathProcessor().isReadonly(path)) {
            if (!Confirmation.confirm(getPanel(),
                    new ResourceKey("confirm.rename.readonly", path.toAbsolutePath())
            ))
            return;
        }

        RenameDialog dlgRename = new RenameDialog(getTable().getWrapper(), context.getPathProcessor(), path);
        dlgRename.setVisibleRelativeToParent(1.0, null);

        final Path newPath = dlgRename.getNewPath();
        if (newPath == null) return;
        try {
            if (context.isZip()) {
                context.getZipWorker().rename(path, newPath);

                //renaming is possible if only one row is selected
                getTable().addListener(new XTableAdapter() {
                    @Override
                    public void workingDirectoryChanged(XTableContext newContext) {
                        if (context.getDirectory().equals(newContext.getDirectory())) {
                            //select renamed path
                            getTable().setSelectedPaths(newPath);
                        }
                        getTable().removeListener(this);
                    }
                });
            } else {
                Files.move(path, newPath, StandardCopyOption.REPLACE_EXISTING, LinkOption.NOFOLLOW_LINKS);
                XTable passivePanel = XPanels.getOtherPanel(getPanel()).getTable();
                if (path.equals(passivePanel.getContext())) {
                    Path[] paths = passivePanel.getSelectedPaths();
                    passivePanel.setContextDirectory(newPath);
                    passivePanel.setSelectedPaths(paths);
                }

                //renaming is possible if only one row is selected
                getTable().addListener(new XTableAdapter() {
                    @Override
                    public void pathCreated(Path path) {
                        if (path.equals(newPath)) {
                            //select renamed path
                            getTable().setSelectedPaths(newPath);
                            getTable().removeListener(this);
                        }
                    }

                    @Override
                    public void workingDirectoryChanged(XTableContext newContext) {
                        getTable().removeListener(this);
                    }
                });
            }

        } catch (IOException e) {
            Errors.showError(e);
        }
    }
}

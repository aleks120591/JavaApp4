package lenacom.filer.action;

import lenacom.filer.config.XIcon;
import lenacom.filer.config.ResourceKey;
import lenacom.filer.message.Errors;
import lenacom.filer.panel.XPanel;
import lenacom.filer.panel.XPanels;
import lenacom.filer.panel.XTable;
import lenacom.filer.panel.XTableContext;
import lenacom.filer.path.PathUtils;
import lenacom.filer.path.processor.RenameDialog;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

public class CopyAction extends CopyMoveAction implements ContextAction {
    private static final Set<SelectionType> types = SelectionType.getAllExceptParentDirectory();

    @Override
    public Set<SelectionType> getSelectionTypes() {
        return types;
    }

    public CopyAction(String key, boolean withEllipsis) {
        super(key, withEllipsis);
        this.setIcon(XIcon.COPY.getIcon());
    }

    @Override
    public boolean isEnabled() {
        SelectionType type = SelectionType.getSelectionType(getTable());
        return types.contains(type);
    }

    @Override
    public void act() {
        final Path[] source = getTable().getSelectedPaths();
        if (source == null || source.length == 0) return;

        final XPanel pnlPassive = XPanels.getOtherPanel(getPanel());
        final XTableContext activeContext = getTable().getContext();
        final XTableContext passiveContext = pnlPassive.getTable().getContext();

        if (source.length == 1 && !activeContext.isZip() && activeContext.getDirectory().equals(passiveContext.getDirectory())) {
            Path target = passiveContext.getDirectory().resolve(PathUtils.getName(source[0]));
            RenameDialog dlgRename = new RenameDialog(getTable().getWrapper(),
                    new ResourceKey("confirm.copy.and.rename"),
                    new ResourceKey("dlg.rename.lbl.name"),
                    activeContext.getPathProcessor(), target);
            dlgRename.setOkEnabled(true);
            dlgRename.setVisibleRelativeToParent(1.0, null);
            target = dlgRename.getNewPath();
            if (target != null) {
                new CopyWorker(source[0], target).execute();
            }
        } else {
            ConfirmPathsDialog dlg = new ConfirmPathsDialog(getTable().getWrapper(),
                    new ResourceKey("confirm.title"),
                    new ResourceKey("confirm.copy.files"), source) {
                @Override
                protected boolean onOk() {
                    process(activeContext, source, passiveContext);
                    return true;
                }
            };
            dlg.setVisibleRelativeToParent();
        }
    }

    @Override
    protected void process(XTableContext sourceContext, Path[] source, XTableContext targetContext) {
        if (source == null || source.length == 0) return;
        Path targetDirectory = targetContext.getDirectory();

        try {
            if (sourceContext.isZip() && targetContext.isZip()) {
                sourceContext.getZipWorker().copyOut(targetContext.getZipWorker(), source, targetDirectory);
            } else if (sourceContext.isZip()) {
                sourceContext.getZipWorker().copyOut(source, targetDirectory);
            } else if (targetContext.isZip()) {
                targetContext.getZipWorker().copyIn(source, targetDirectory);
            } else {
                new CopyWorker(source, targetDirectory).execute();
            }
        } catch (IOException e) {
            Errors.showError(e);
        }
    }

    @Override
    protected void process(Path[] source, XTable tblTarget) {
        process("confirm.copy.files", source, tblTarget);
    }

    public void copy(Path[] source, XTable tblTarget) {
        process(source, tblTarget);
    }
}

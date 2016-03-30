package lenacom.filer.action;

import lenacom.filer.component.TextDialog;
import lenacom.filer.config.XIcon;
import lenacom.filer.config.ResourceKey;
import lenacom.filer.message.Errors;
import lenacom.filer.message.Messages;
import lenacom.filer.panel.PathRow;
import lenacom.filer.panel.XPanel;
import lenacom.filer.panel.XPanels;
import lenacom.filer.panel.XTable;
import lenacom.filer.path.PathUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

class SymlinkAction extends XAction implements ContextAction {
    private static final Set<SelectionType> types;

    static {
        types = new HashSet<>();
        if (PathUtils.isSymlinkSupported()) {
            types.add(SelectionType.DIRECTORY);
            types.add(SelectionType.FILE);
            types.add(SelectionType.ZIP);
        }
    }

    @Override
    public Set<SelectionType> getSelectionTypes() {
        return types;
    }

    public SymlinkAction(String key, boolean withEllipsis) {
        super(key, withEllipsis);
        this.setIcon(XIcon.SYMLINK.getIcon());
    }

    @Override
    public boolean isEnabled() {
        SelectionType type = SelectionType.getSelectionType(getTable());
        if (!types.contains(type)) return false;
        XPanel otherPanel = XPanels.getOtherPanel(getPanel());
        return !getTable().getContext().isZip() && !otherPanel.getTable().getContext().isZip();
    }

    @Override
    public void act() {
        if (!isEnabled()) return;
        PathRow row = getTable().getFocusedRow();
        if (!row.isSymlink()) {
            new NewSymlinkDialog(getPanel(), row.getPath());
        }
    }

    private final static class NewSymlinkDialog extends TextDialog {
        private XPanel xpnl;
        private Path target;

        NewSymlinkDialog(XPanel xpnl, Path target) {
            super(xpnl.getTable().getWrapper(),
                    new ResourceKey("dlb.symlink.title"),
                    new ResourceKey("dlb.symlink.lbl.name"),
                    new ResourceKey("dlb.symlink.lbl.descr", target)
                    );
            this.setText(target.getFileName().toString());
            this.xpnl = xpnl;
            this.target = target;
            setVisibleRelativeToParent(1.0, null);
        }

        @Override
        protected boolean onOk() {
            XTable tblPassive = XPanels.getOtherPanel(xpnl).getTable();
            Path symlink = tblPassive.getContext().getDirectory().resolve(getText());

            if (PathUtils.existsNoFollowLink(symlink)) {
                Messages.showMessage(NewSymlinkDialog.this, "err.path.already.exists", symlink.toAbsolutePath());
                return false;
            }

            try {
                Files.createSymbolicLink(symlink, target);
                return true;
            } catch (IOException e) {
                Errors.showError(NewSymlinkDialog.this, e);
                return false;
            }
        }
    }
}

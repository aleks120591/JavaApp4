package lenacom.filer.action;

import lenacom.filer.component.TextDialog;
import lenacom.filer.config.ResourceKey;
import lenacom.filer.config.XIcon;
import lenacom.filer.message.Messages;
import lenacom.filer.panel.XTable;
import lenacom.filer.path.PathUtils;

import java.nio.file.Path;
import java.nio.file.Paths;

class DirectoryAsTextAction extends XAction {

    public DirectoryAsTextAction(String key, boolean withEllipsis) {
        super(key, withEllipsis);
        this.setIcon(XIcon.TEXT.getIcon());
    }

    @Override
    public void act() {
        new DirectoryAsTextDialog(getTable());
    }

    private class DirectoryAsTextDialog extends TextDialog {
        private XTable xtbl;

        public DirectoryAsTextDialog(XTable xtbl) {
            super(xtbl.getWrapper(), new ResourceKey("dlg.dir.as.text.title"), new ResourceKey("dlg.dir.as.text.lbl.path"));
            this.xtbl = xtbl;
            this.setVisibleRelativeToParent(1.0, null);
        }

        @Override
        protected boolean onOk() {
            String text = getText();
            Path path = Paths.get(text);
            if (!PathUtils.existsFollowLink(path)) {
                path = Paths.get(text.replaceAll("/", "\\"));
                if (!PathUtils.existsFollowLink(path)) {
                    path = Paths.get(text.replaceAll("\\\\", "/"));
                    if (!PathUtils.existsFollowLink(path)) {
                        Messages.showMessage(DirectoryAsTextDialog.this, "dlg.dir.as.text.err.failed.open", text);
                        return false;
                    }
                }
            }
            xtbl.setContextDirectory(path);
            return true;
        }
    }
}

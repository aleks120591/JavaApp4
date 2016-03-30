package lenacom.filer.action;

import lenacom.filer.config.ResourceKey;
import lenacom.filer.message.Messages;
import lenacom.filer.panel.XTable;
import lenacom.filer.panel.XTableContext;
import lenacom.filer.path.PathUtils;
import lenacom.filer.zip.ZipWorker;

import java.nio.file.Path;

abstract class CopyMoveAction extends XAction {

    public CopyMoveAction(String key, boolean withEllipsis) {
        super(key, withEllipsis);
    }

    protected void process(String keyConfirmMessage, final Path[] source, final XTable tblTarget) {
        if (source == null || source.length == 0) return;

        ConfirmPathsDialog dlg = new ConfirmPathsDialog(getTable().getWrapper(),
                new ResourceKey("confirm.title"),
                new ResourceKey(keyConfirmMessage), source) {
            @Override
            protected boolean onOk() {
                confirmedProcess(source, tblTarget);
                return true;
            }
        };
        dlg.setVisibleRelativeToParent();
    }

    private void confirmedProcess(Path[] source, XTable tblTarget) {
        Path sourceDirectory = source[0].getParent();
        for (Path path: source) assert(sourceDirectory.equals(path.getParent()));
        if (sourceDirectory.equals(tblTarget.getContext().getDirectory())) return;

        XTableContext sourceContext;
        if (PathUtils.isZip(sourceDirectory)) {
            sourceContext = new XTableContext(sourceDirectory, ZipWorker.create(sourceDirectory));
        } else if (!PathUtils.existsNoFollowLink(sourceDirectory)) {
            Path zip = PathUtils.getClosestExistentParent(sourceDirectory);
            if (PathUtils.isZip(zip)) {
                sourceContext = new XTableContext(sourceDirectory, ZipWorker.create(zip));
            } else {
                Messages.showMessage("err.path.does.not.exist", sourceDirectory);
                return;
            }
        } else {
            sourceContext = new XTableContext(sourceDirectory, null);
        }
        process(sourceContext, source, tblTarget.getContext());
    }

    protected abstract void process(Path[] source, XTable tblTarget);
    protected abstract void process(XTableContext sourceContext, Path[] source, XTableContext targetContext);
}

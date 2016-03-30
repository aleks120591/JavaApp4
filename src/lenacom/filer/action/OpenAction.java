package lenacom.filer.action;

import lenacom.filer.message.Errors;
import lenacom.filer.message.Messages;
import lenacom.filer.panel.*;
import lenacom.filer.path.PathUtils;
import lenacom.filer.zip.ExtractedTmpFile;

import java.awt.*;
import java.nio.file.Path;
import java.util.Set;

public class OpenAction extends XAction implements ContextAction {
    private static final Set<SelectionType> types;

    static {
        types = SelectionType.getAllExceptMultiple();
        if (!PathUtils.isOpenSupported()) {
            types.remove(SelectionType.FILE);
            types.remove(SelectionType.SYMLINK_FILE);
        }
    }

    @Override
    public Set<SelectionType> getSelectionTypes() {
        return types;
    }

    public OpenAction(String key, boolean withEllipsis) {
        super(key, withEllipsis);
    }

    @Override
    public boolean isEnabled() {
        SelectionType type = SelectionType.getSelectionType(getTable());
        return types.contains(type);
    }

    @Override
    public void act() {
        open();
    }

    private void open() {
        PathRow row = getTable().getFocusedRow();
        Path path = row.getPath();
        if (row instanceof DirectoryRow || row instanceof FileRow && ((FileRow) row).isZip()) {
            XTableListener listener = null;
            try {
                listener = new XTableAdapter() {
                    @Override
                    public void startWaiting() {
                        getPanel().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    }
                };
                getTable().addListener(listener);
                Path closedDirectory = getTable().getContext().getDirectory();
                PathRow parentRow = getTable().getParentRow(); //before setting the new dir
                getTable().setContextDirectory(row.getPath());
                if (row == parentRow) {
                    //select just closed child dir
                    //we can be returning from tmp zip
                    getTable().setSelectedPaths(row.getPath().resolve(closedDirectory.getFileName()));
                }
            } finally {
                if (listener != null) getTable().removeListener(listener);
                getPanel().setCursor(Cursor.getDefaultCursor());
            }
        }
        else {
            open(getTable().getContext(), path);
        }
    }

    public void open(XTableContext context, Path path) {
        if (PathUtils.isOpenSupported()) {
            try {
                if (context.isZip()) {
                    ExtractedTmpFile tmpFile = context.getZipWorker().extractTempFile(path);
                    path = tmpFile.getFile();
                    tmpFile.watchChangesAndUpdateZipImmediately();
                } else if (!PathUtils.existsFollowLink(path)) {
                    Messages.showMessage("err.path.does.not.exist", path.toAbsolutePath());
                    return;
                }
                Desktop.getDesktop().open(path.toFile());
            } catch (Exception x) {
                Errors.showError(x);
            }
        }
    }
}

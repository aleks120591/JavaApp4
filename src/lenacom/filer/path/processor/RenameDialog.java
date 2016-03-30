package lenacom.filer.path.processor;

import lenacom.filer.config.ResourceKey;
import lenacom.filer.message.Confirmation;
import lenacom.filer.component.TextDialog;

import java.awt.*;
import java.nio.file.Path;

public class RenameDialog extends TextDialog {
    private final PathProcessor pp;
    //this field is accessed from different threads
    private volatile Path newPath;
    private Path path;

    public RenameDialog(Component owner, ResourceKey keyTitle, ResourceKey keyLabel, PathProcessor pp, Path path) {
        super(owner, keyTitle, keyLabel, path.getFileName().toString());
        this.pp = pp;
        this.path = path;
    }

    public RenameDialog(Component owner, PathProcessor pp, Path path) {
        this(owner, new ResourceKey("dlg.rename.title"), new ResourceKey("dlg.rename.lbl.name"), pp, path);
    }

    @Override
    protected boolean onOk() {
        newPath = path.resolveSibling(getText().trim());

        if (pp.fileExists(newPath)) {
            if (!Confirmation.confirm(this, new ResourceKey("err.path.already.exists.overwrite", newPath))) {
                return false;
            }
        }

        return true;
    }

    @Override
    protected boolean onCancel() {
        newPath = null;
        return true;
    }

    public Path getNewPath() {
        return newPath;
    }
}

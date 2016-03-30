package lenacom.filer.action.find;

import lenacom.filer.config.ResourceKey;
import lenacom.filer.message.Confirmation;
import lenacom.filer.panel.XTable;

import java.nio.file.Path;

class OtherTableHandler {
    private FindDialog dlgFind;
    private XTable tblOther;
    private Path originalDirectory;
    private boolean originalDirectoryChanged;

    OtherTableHandler(FindDialog dlgFind, XTable tblOther) {
        this.dlgFind = dlgFind;
        this.tblOther = tblOther;
        this.originalDirectory = tblOther.getContext().getDirectory();
    }

    XTable getOtherTable() {
        return tblOther;
    }

    void restoreOriginalDirectory() {
        if (originalDirectoryChanged) {
            Path newOtherPanelDirectory = tblOther.getContext().getDirectory();
            if (!newOtherPanelDirectory.equals(originalDirectory)) {
                if (Confirmation.confirm(dlgFind,
                        new ResourceKey("dlg.find.confirm.other.pnl.dir", originalDirectory)
                )) {
                    tblOther.setContextDirectory(originalDirectory);
                }
            }
        }
    }

    void checkDirectoryChanged() {
        if (!originalDirectory.equals(tblOther.getContext().getDirectory())) {
            //we set this flag because the other panel directory can be changed manually
            originalDirectoryChanged = true;
        }
    }
}

package lenacom.filer.action;

import lenacom.filer.config.ResourceKey;
import lenacom.filer.message.Confirmation;
import lenacom.filer.config.Configuration;
import lenacom.filer.message.Messages;
import lenacom.filer.root.FileOperationWorkers;
import lenacom.filer.root.RootFrame;

class RestoreDefaultsAction extends XAction {

    public RestoreDefaultsAction(String key, boolean withEllipsis) {
        super(key, withEllipsis);
    }

    @Override
    public void act() {
        int count = FileOperationWorkers.count();
        if (count > 0) {
            Messages.showMessage("msg.file.operations.running", count);
            return;
        }
        if (Confirmation.confirm(new ResourceKey("confirm.restore.defaults"))) {
            Configuration.clear();
            RootFrame.restart();
        }
    }
}

package lenacom.filer.message;

import lenacom.filer.component.HtmlLabel;
import lenacom.filer.component.OkCancelDialog;
import lenacom.filer.config.ResourceKey;
import lenacom.filer.root.RootFrame;

import java.awt.*;

public class Confirmation {

    public static boolean confirm(ResourceKey keyMessage) {
        InnerConfirmDialog dlg = new InnerConfirmDialog(RootFrame.getRoot(), new ResourceKey("confirm.title"), keyMessage);
        dlg.setVisibleRelativeToParent();
        return dlg.isOk();
    }

    public static boolean confirm(Component owner, ResourceKey keyMessage) {
        InnerConfirmDialog dlg = new InnerConfirmDialog(owner, new ResourceKey("confirm.title"), keyMessage);
        dlg.setVisibleRelativeToParent();
        return dlg.isOk();
    }

    private static final class InnerConfirmDialog extends OkCancelDialog {
        private boolean ok = false;
        private HtmlLabel lblMessage;

        private InnerConfirmDialog(Component owner, ResourceKey keyTitle, ResourceKey keyMessage) {
            super(owner, keyTitle, "btn.yes", "btn.no");
            lblMessage = new HtmlLabel(keyMessage);
            this.setCenterComponent(lblMessage);
        }

        @Override
        protected boolean onOk() {
            ok = true;
            return true;
        }

        public boolean isOk() {
            return ok;
        }

        @Override
        public void pack() {
            super.pack();
            int diff = lblMessage.adjustHeight();
            this.setHeight(getHeight() + diff);
        }
    }

}

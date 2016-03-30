package lenacom.filer.component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public abstract class ApplyDialog extends ButtonsDialog {
    private JButton btnApply;
    private JButton btnApplyAndClose;

    public ApplyDialog(Component owner, String title) {
        super(owner, title);
        this.setModalityType(Dialog.ModalityType.MODELESS);

        JButton btnCancel = this.createCancelButton("btn.cancel");

        btnApply = this.createDefaultButton("btn.apply");
        btnApply.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                apply();
            }
        });
        btnApply.setEnabled(false);

        btnApplyAndClose = XButton.create("btn.apply.and.close");
        btnApplyAndClose.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (apply()) dispose();
            }
        });
        btnApplyAndClose.setEnabled(false);

        this.addRightButtons(btnApply, btnApplyAndClose, btnCancel);
    }

    public void setApplyEnabled(boolean enabled) {
        btnApply.setEnabled(enabled);
        btnApplyAndClose.setEnabled(enabled);
    }

    protected abstract boolean apply();
}

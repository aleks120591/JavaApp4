package lenacom.filer.action.text;

import lenacom.filer.component.ButtonsDialog;
import lenacom.filer.component.XButton;
import lenacom.filer.component.XLabel;
import lenacom.filer.config.ResourceKey;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

abstract class YesNoCancelDialog extends ButtonsDialog implements ActionListener {
    private JButton btnYes, btnNo, btnCancel;

    public YesNoCancelDialog(Component owner, ResourceKey keyTitle, ResourceKey keyDescription) {
        super(owner, keyTitle);
        this.setCenterComponent(XLabel.create(keyDescription));

        btnYes = createDefaultButton("btn.yes");
        btnYes.addActionListener(this);
        btnNo = XButton.create("btn.no");
        btnNo.addActionListener(this);
        btnCancel = createCancelButton();
        btnCancel.addActionListener(this);

        this.addRightButtons(btnYes, btnNo, btnCancel);
        this.setVisibleRelativeToParent();
    }

    protected abstract void onYes();
    protected abstract void onNo();

    @Override
    public void actionPerformed(ActionEvent e) {
        JButton btn = (JButton) e.getSource();
        if (btn.equals(btnYes)) onYes();
        else if (btn.equals(btnNo)) onNo();
        else if (btn.equals(btnCancel)) onCancel();
        dispose();
    }
}

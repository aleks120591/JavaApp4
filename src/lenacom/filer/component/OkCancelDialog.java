package lenacom.filer.component;

import lenacom.filer.config.ResourceKey;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public abstract class OkCancelDialog extends ButtonsDialog {
    private JButton btnOk;

    public OkCancelDialog(Component owner, String keyTitle) {
        this(owner, new ResourceKey(keyTitle));
    }

    public OkCancelDialog(Component owner, ResourceKey keyTitle) {
        super(owner, keyTitle);
        init("btn.ok", "btn.cancel");
    }

    public OkCancelDialog(Component owner, ResourceKey keyTitle, String keyOk, String keyCancel) {
        super(owner, keyTitle);
        init(keyOk, keyCancel);
    }

    private void init(String keyOk, String keyCancel) {
        btnOk = createDefaultButton(keyOk);
        btnOk.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (onOk()) dispose();
            }
        });
        this.addRightButtons(btnOk, createCancelButton(keyCancel));
    }

    public void setOkEnabled(boolean enabled) {
        btnOk.setEnabled(enabled);
    }

    protected abstract boolean onOk();

    protected boolean onCancel() {
        //do nothing
        return true;
    }
}

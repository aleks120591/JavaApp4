package lenacom.filer.action;

import lenacom.filer.component.ButtonsDialog;
import lenacom.filer.component.EditableDocumentListener;
import lenacom.filer.component.XTextField;
import lenacom.filer.panel.XTable;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

abstract class NewPathDialog extends ButtonsDialog {
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyy-MM-dd");
    protected XTable xtbl;
    private int pos;
    protected JButton btnCreate, btnCancel;
    protected XTextField textField = new XTextField();

    NewPathDialog(XTable xtbl, String kewTitle) {
        super(xtbl.getWrapper(), kewTitle);
        this.xtbl = xtbl;

        textField.addCaretListener(new CaretListener() {
            @Override
            public void caretUpdate(CaretEvent e) {
                assert(SwingUtilities.isEventDispatchThread());
                pos = e.getDot();
            }
        });

        textField.getDocument().addDocumentListener(new EditableDocumentListener() {
            @Override
            protected void onEveryUpdate() {
                onTextChanged();
            }
        });

        final String date = DATE_FORMAT.format(new Date(System.currentTimeMillis()));
        JButton btnDate = new JButton(date);
        ActionListener listener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                insertText(date);
            }
        };
        btnDate.addActionListener(listener);
        registerButton(btnDate, KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK);
        btnDate.setToolTipText("Ctrl-D");

        addLeftButtons(btnDate);

        btnCreate = createDefaultButton("dlg.new.path.btn.create");
        btnCreate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (onCreate()) dispose();
            }
        });
        setCreateEnabled(false);
        btnCancel = createCancelButton("btn.cancel");
    }

    protected void insertText(String insertedText) {
        String text = textField.getText();
        int originalPos = pos;
        String newText = text.substring(0, pos) + insertedText + text.substring(pos);
        textField.setText(newText);
        textField.requestFocus();
        textField.setCaretPosition(originalPos + insertedText.length());
    }

    protected void onTextChanged() {
        setCreateEnabled(!getText().isEmpty());
    }

    protected String getText() {
        return textField.getText();
    }

    public void setCreateEnabled(boolean enabled) {
        btnCreate.setEnabled(enabled);
    }

    protected abstract boolean onCreate();

    protected boolean onCancel() {
        //do nothing
        return true;
    }
}

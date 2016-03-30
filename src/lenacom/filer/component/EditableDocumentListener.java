package lenacom.filer.component;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public abstract class EditableDocumentListener implements DocumentListener {
    @Override
    public void insertUpdate(DocumentEvent e) {
        onEveryUpdate();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        onEveryUpdate();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        onEveryUpdate();
    }

    protected abstract void onEveryUpdate();
}

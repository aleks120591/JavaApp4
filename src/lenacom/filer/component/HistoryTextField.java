package lenacom.filer.component;

import lenacom.filer.config.History;

import javax.swing.*;
import javax.swing.text.Document;
import java.awt.event.*;

public abstract class HistoryTextField extends XTextField {
    private History history;
    private String currentValue;
    private int index;

    private EditableDocumentListener editableDocumentListener = new EditableDocumentListener() {
        @Override
        protected void onEveryUpdate() {
            HistoryTextField.this.onEveryUpdate();
            String value = getText();
            index = -1;
            currentValue = value;
        }
    };

    public HistoryTextField(History history) {
        this.history = history;
        currentValue = "";
        index = -1; //-1 means current value
        Document doc = getDocument();

        doc.addDocumentListener(editableDocumentListener);

        this.addHierarchyListener(new HierarchyListener() {
            public void hierarchyChanged(HierarchyEvent e) {
                if ((HierarchyEvent.SHOWING_CHANGED & e.getChangeFlags()) != 0
                        && HistoryTextField.this.isShowing()) {
                    HistoryTextField.this.onEveryUpdate(); //execute first time after showing up
                }
            }
        });

        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    if (index > -1) index--;
                    if (index == 0 && currentValue.equals(HistoryTextField.this.history.getItemAt(0))) {
                        index--;
                    }
                    setHistoryValue();
                } else if (e.getKeyCode() == KeyEvent.VK_UP) {
                    if (index < HistoryTextField.this.history.getSize() - 1) index++;
                    if (index == 0 && currentValue.equals(HistoryTextField.this.history.getItemAt(0)) &&
                        index < HistoryTextField.this.history.getSize() - 1) {
                        index++;
                    }
                    setHistoryValue();
                }
            }
        });
    }

    private void setHistoryValue() {
         if (index == -1) {
             setText(currentValue);
         } else {
             this.getDocument().removeDocumentListener(editableDocumentListener);
             setText(history.getItemAt(index));
             onEveryUpdate();
             this.getDocument().addDocumentListener(editableDocumentListener);
         }
    }

    protected abstract void onEveryUpdate();

    public void addHistoryItem(String item) {
        assert(SwingUtilities.isEventDispatchThread());
        history.addItemOnTop(item);
    }
}

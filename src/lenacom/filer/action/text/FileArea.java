package lenacom.filer.action.text;

import lenacom.filer.config.Colors;
import lenacom.filer.config.Configuration;
import lenacom.filer.config.Fonts;
import lenacom.filer.message.Errors;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.undo.UndoManager;

class FileArea extends JTextArea {
    private boolean editable;
    private TextDocument doc;
    private UndoManager undo = new UndoManager();
    private AttributeSet attrs = new SimpleAttributeSet();

    FileArea(String text, boolean editable) {
        doc = new TextDocument();
        setText(text);
        //add undoable listener after setting the text
        doc.addUndoableEditListener(undo);
        this.editable = editable;

        this.setTabSize(4);

        Boolean lineWrap = Configuration.getBoolean(Configuration.WRAP_LINES, Boolean.FALSE);
        setLineWrap(lineWrap);

        setDocument(doc);
        setWrapStyleWord(true);

        init();
    }

    boolean canUndo() {
        return undo.canUndo();
    }

    void undo() {
        if (undo.canUndo()) undo.undo();
    }

    void redo() {
        if (undo.canRedo()) undo.redo();
    }

    boolean canRedo() {
        return undo.canRedo();
    }

    private void init() {
        setFont(Fonts.getFont());
        setBackground(Colors.getBackground());
        setForeground(Colors.getForeground());
        setSelectionColor(Colors.getForeground());
        setSelectedTextColor(Colors.getBackground());
        setCaretColor(Colors.getForeground());
    }

    void refresh() {
         init();
    }

    //used in viewing files too for initial setting and changing charsets
    @Override
    public void setText(String text) {
        assert(SwingUtilities.isEventDispatchThread());
        boolean rememberEditable = editable;
        editable = true;
        doc.setText(text);
        editable = rememberEditable;
        setCaretPosition(0);
    }

    synchronized boolean replace(int pos, String needle, String replacement, boolean caseSensitive) throws BadLocationException {
        TextDocument doc = (TextDocument) this.getDocument();
        String testNeedle = doc.getText(pos, needle.length());
        if (caseSensitive && !testNeedle.equals(needle) ||
                !caseSensitive && !testNeedle.equalsIgnoreCase(needle)) {
            return false;
        }
        doc.replace(pos, needle.length(), replacement, attrs);
        return true;
    }

    private final class TextDocument extends DefaultStyledDocument {

        @Override
        public void remove(int offs, int len) throws BadLocationException {
            if (editable) {
                super.remove(offs, len);
            }
        }

        @Override
        public void insertString(int offset, String str, AttributeSet attrs) throws BadLocationException {
            if (editable) {
                super.insertString(offset, str, attrs);
            }
        }

        public void setText(String str) {
            try {
                if (this.getLength() == 0) {
                    super.insertString(0, str, attrs);
                } else {
                    super.replace(0, this.getLength(), str, attrs);
                    //calls insertString
                }
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }
    }

}

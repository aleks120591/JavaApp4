package lenacom.filer.component;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

public class NumberTextField extends XTextField {

    public NumberTextField(String text, int columns) {
        this.setDocument(new NumberDocument());
        this.setColumns(columns);
        this.setText(text); //set text after doc and columns
    }

    private class NumberDocument extends PlainDocument {

        @Override
        public void insertString(int offset, String str, AttributeSet attrs) throws BadLocationException {
            str = str.replaceAll("[^0-9]*", "");
            int maxLength = getColumns() - getLength();
            if (str.length() > maxLength) str = str.substring(0, maxLength);
            super.insertString(offset, str, attrs);
        }
    }
}

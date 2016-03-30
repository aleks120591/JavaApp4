package lenacom.filer.component;

import lenacom.filer.config.Colors;
import lenacom.filer.config.Fonts;

import javax.swing.*;

public class XTextField extends JTextField {

    {
        init();
    }

    private void init() {
        this.setFont(Fonts.getFont());
        this.setBackground(Colors.getBackground());
        this.setForeground(Colors.getForeground());
        this.setSelectionColor(Colors.getForeground());
        this.setSelectedTextColor(Colors.getBackground());
        this.setCaretColor(Colors.getForeground());
    }

    public XTextField() {
    }

    public XTextField(String text) {
        super(text);
    }

    public void refresh() {
        init();
    }
}

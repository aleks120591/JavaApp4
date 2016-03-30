package lenacom.filer.component;

import lenacom.filer.config.History;

public abstract class ExtendedHistoryTextField extends HistoryTextField {

    public ExtendedHistoryTextField(History history) {
        super(history);
    }

    public void setText(String text) {
        text = text.replace("\t", "\\t").replace("\n", "\\n").replace("\r", "\\r");
        super.setText(text);
    }

    public String getText() {
        String text = super.getText();
        return text.replace("\\t", "\t").replace("\\n", "\n").replace("\\r", "\r");
    }
}

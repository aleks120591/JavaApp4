package lenacom.filer.component;

import lenacom.filer.config.ResourceKey;
import lenacom.filer.config.Resources;

import javax.swing.*;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.text.View;
import java.awt.*;

public class HtmlLabel extends JLabel {

    public HtmlLabel(ResourceKey key) {
        String text = Resources.getMessage(key);

        Character mnemonic = Resources.getMnemonic(key);
        if (mnemonic != null) {
            setDisplayedMnemonic(mnemonic);
            text = text.replaceFirst(mnemonic.toString(), "<u>" + mnemonic + "</u>");
        }

        text = text.replace("\n", "<br>");
        setText("<html>" + text + "<html>");
    }

    public int adjustHeight() {
        Dimension oldSize = getSize();
        View view = (View) getClientProperty(BasicHTML.propertyKey);
        view.setSize(oldSize.width, 0.0f);
        int width = (int) view.getPreferredSpan(View.X_AXIS);
        int height = (int) view.getPreferredSpan(View.Y_AXIS);
        setSize(width,  height);
        return height - oldSize.height;
    }
}

package lenacom.filer.component;

import lenacom.filer.config.ResourceKey;
import lenacom.filer.config.Resources;

import javax.swing.*;

public class XLabel {

    public static JLabel createWithTooltipAndColon(ResourceKey key, String tooltip) {
        JLabel label = new JLabel();
        Character mnemonic = Resources.getMnemonic(key);
        String text = Resources.getMessage(key);
        if (mnemonic != null) {
            text = text.replaceFirst(mnemonic.toString(), "<u>" + mnemonic + "</u>");
            label.setDisplayedMnemonic(mnemonic);
        }
        label.setText("<html>" + text.replace("\n", "<br>") + "<sup>*</sup>:</html>");
        label.setToolTipText(tooltip);
        return label;
    }

    public static JLabel createWithTooltipAndColon(ResourceKey key, ResourceKey keyTooltip) {
        return createWithTooltipAndColon(key, Resources.getMessage(keyTooltip));
    }

    public static JLabel create(String key) {
        return create(Resources.getMessage(key), Resources.getMnemonic(key));
    }

    public static JLabel create(ResourceKey key) {
        return create(Resources.getMessage(key), Resources.getMnemonic(key));
    }

    public static JLabel createWithColon(String key) {
        return createWithColon(new ResourceKey(key));
    }

    public static JLabel createWithColon(ResourceKey key) {
        String text = Resources.getMessage(key);
        if (!text.isEmpty()) text += ":";
        return create(text, Resources.getMnemonic(key));
    }

    private static JLabel create(String text, Character mnemonic) {
        JLabel lbl = new JLabel(text);
        if (mnemonic != null) lbl.setDisplayedMnemonic(mnemonic);
        return lbl;
    }

}

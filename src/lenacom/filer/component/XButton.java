package lenacom.filer.component;

import lenacom.filer.config.ResourceKey;
import lenacom.filer.config.Resources;

import javax.swing.*;

public class XButton {

    public static JButton create(String key) {
        return create(Resources.getMessage(key), Resources.getMnemonic(key));
    }

    public static JButton create(ResourceKey key) {
        return create(Resources.getMessage(key), Resources.getMnemonic(key));
    }

    public static JButton createWithEllipsis(ResourceKey key) {
        return create(Resources.getMessageWithEllipsis(key), Resources.getMnemonic(key));
    }

    private static JButton create(String text, Character mnemonic) {
        JButton btn = new JButton(text);
        if (mnemonic != null) btn.setMnemonic(mnemonic);
        return btn;
    }
}

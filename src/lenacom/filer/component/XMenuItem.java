package lenacom.filer.component;

import lenacom.filer.config.Resources;

import javax.swing.*;

public class XMenuItem extends JMenuItem {
    public XMenuItem(String key) {
        super(Resources.getMessage(key));
        setMnemonic(key);
    }

    public XMenuItem(String key, boolean withEllipsis) {
        super(withEllipsis? Resources.getMessageWithEllipsis(key) : Resources.getMessage(key));
        setMnemonic(key);
    }

    private void setMnemonic(String key) {
        Character mnemonic = Resources.getMnemonic(key);
        if (mnemonic != null) setMnemonic(mnemonic);
    }
}

package lenacom.filer.component;

import lenacom.filer.config.Resources;

import javax.swing.*;

public class XMenu extends JMenu {
    public XMenu(String key) {
        super(Resources.getMessage(key));
        Character mnemonic = Resources.getMnemonic(key);
        if (mnemonic != null) setMnemonic(mnemonic);
    }
}

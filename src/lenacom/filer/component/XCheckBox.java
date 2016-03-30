package lenacom.filer.component;

import lenacom.filer.config.ResourceKey;
import lenacom.filer.config.Resources;

import javax.swing.*;

public class XCheckBox extends JCheckBox {
    public XCheckBox(String key) {
        this(new ResourceKey(key), false);
    }

    public XCheckBox(ResourceKey key) {
        this(key, false);
    }

    public XCheckBox(String key, boolean selected) {
        this(new ResourceKey(key), selected);
    }

    public XCheckBox(ResourceKey key, boolean selected) {
        super(Resources.getMessage(key), selected);
        Character mnemonic = Resources.getMnemonic(key);
        if (mnemonic != null) this.setMnemonic(mnemonic);
    }
}

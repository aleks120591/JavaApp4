package lenacom.filer.component;

import lenacom.filer.config.ResourceKey;
import lenacom.filer.config.Resources;

import javax.swing.*;

public class XRadioButton extends JRadioButton {

    public XRadioButton(String key) {
        this(new ResourceKey(key), false);
    }

    public XRadioButton(ResourceKey key) {
        this(key, false);
    }

    public XRadioButton(String key, boolean selected) {
        this(new ResourceKey(key), selected);
    }

    public XRadioButton(ResourceKey key, boolean selected) {
        super(Resources.getMessage(key), selected);
        Character mnemonic = Resources.getMnemonic(key);
        if (mnemonic != null) this.setMnemonic(mnemonic);
    }
}

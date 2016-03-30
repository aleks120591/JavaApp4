package lenacom.filer.component;

import lenacom.filer.config.ResourceKey;
import lenacom.filer.config.Resources;

import javax.swing.*;

public class XToggleButton extends JToggleButton {

    protected XToggleButton() {
    }

    public XToggleButton(String key) {
        this(new ResourceKey(key), false);
    }

    public XToggleButton(ResourceKey key, boolean selected) {
        this.setText(key);
        this.setSelected(selected);
    }

    protected void setText(ResourceKey key) {
        this.setText(Resources.getMessage(key));
        Character mnemonic = Resources.getMnemonic(key);
        if (mnemonic != null) this.setMnemonic(mnemonic);
    }

}

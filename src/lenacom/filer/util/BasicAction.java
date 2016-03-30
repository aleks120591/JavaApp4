package lenacom.filer.util;

import lenacom.filer.config.ResourceKey;
import lenacom.filer.config.Resources;

import javax.swing.*;

public abstract class BasicAction extends AbstractAction {
    private String name;

    protected BasicAction() {
    }

    public BasicAction(String key, Object... params) {
        name = Resources.getMessage(new ResourceKey(key, params));
        putValue(Action.NAME, name);
        putMnemonic(key);
    }

    public BasicAction(String key, boolean withEllipsis) {
        name = withEllipsis? Resources.getMessageWithEllipsis(key) : Resources.getMessage(key);
        putValue(Action.NAME, name);
        putMnemonic(key);
    }

    private void putMnemonic(String key) {
        Character mnemonic = Resources.getMnemonic(key);
        if (mnemonic != null) {
            putValue(AbstractAction.MNEMONIC_KEY, (int) mnemonic.charValue());
        }
    }

    public String getName() {
        return name;
    }

    protected void setName(String name) {
        this.name = name;
    }

    public KeyStroke getAccelerator() {
        return (KeyStroke) getValue(AbstractAction.ACCELERATOR_KEY);
    }

    public void setAccelerator(int keyCode, int modifiers) {
        putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(keyCode, modifiers));
    }

    public void setAccelerator(int keyCode) {
        putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(keyCode, 0));
    }
}

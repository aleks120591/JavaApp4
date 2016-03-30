package lenacom.filer.component;

import lenacom.filer.config.XIcon;
import lenacom.filer.config.ResourceKey;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MaximizeButton extends XToggleButton implements ActionListener{
    private ResourceKey keyMaximize, keyMinimize;

    public MaximizeButton(String keyMaximize, String keyMinimize) {
        this.keyMaximize = new ResourceKey(keyMaximize);
        this.keyMinimize = new ResourceKey(keyMinimize);
        setIcon(XIcon.MAXIMIZE.getIcon());
        setText(MaximizeButton.this.keyMaximize);
        this.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (isSelected()) {
            setIcon(XIcon.MINIMIZE.getIcon());
            setText(MaximizeButton.this.keyMinimize);
        } else {
            setIcon(XIcon.MAXIMIZE.getIcon());
            setText(MaximizeButton.this.keyMaximize);
        }
    }
}

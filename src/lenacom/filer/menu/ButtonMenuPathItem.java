package lenacom.filer.menu;

import lenacom.filer.panel.XPanel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.file.Path;

class ButtonMenuPathItem extends JMenuItem implements ActionListener {
    protected Path path;
    protected XPanel panel;

    ButtonMenuPathItem(XPanel panel, String name, Path path) {
        super(name);
        this.path = path;
        this.addActionListener(this);
        this.panel = panel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        panel.getTable().setContextDirectory(path);
    }

    Path getPath() {
        return path;
    }
}

package lenacom.filer.menu;

import lenacom.filer.action.XAction;
import lenacom.filer.panel.XPanel;

import javax.swing.*;

class ButtonMenuItem extends JMenuItem {
    ButtonMenuItem(XPanel panel, XAction action) {
        super(action.clone(panel));
        this.setIcon(action.getIcon());
    }
}

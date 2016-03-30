package lenacom.filer.action;

import lenacom.filer.panel.XPanel;
import lenacom.filer.panel.XPanels;
import lenacom.filer.panel.XTable;
import lenacom.filer.util.BasicAction;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class XAction extends BasicAction implements Comparable<XAction>{

    private XPanel contextPanel;
    private KeyStroke secondAccelerator;
    private ImageIcon icon;

    protected XAction() {
    }

    public XAction(String key, boolean withEllipsis) {
        super(key, withEllipsis);
    }

    protected XPanel getPanel() {
        return contextPanel == null? XPanels.getActivePanel() : contextPanel;
    }

    protected XTable getTable() {
        return getPanel().getTable();
    }

    public XAction clone(XPanel contextPanel) {
        XAction contextAction = null;
        try {
            contextAction = (XAction) this.clone();
            contextAction.contextPanel = contextPanel;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return contextAction;
    }

    public void setSecondAccelerator(int keyCode) {
        secondAccelerator = KeyStroke.getKeyStroke(keyCode, 0);
    }

    public void setSecondAccelerator(int keyCode, int modifiers) {
        secondAccelerator = KeyStroke.getKeyStroke(keyCode, modifiers);
    }

    public KeyStroke getSecondAccelerator() {
        return secondAccelerator;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        act();
    }

    public abstract void act();

    @Override
    public int compareTo(XAction otherAction) {
        return this.getName().compareTo(otherAction.getName());
    }

    public ImageIcon getIcon() {
        return icon;
    }

    public void setIcon(ImageIcon icon) {
        this.icon = icon;
    }

}

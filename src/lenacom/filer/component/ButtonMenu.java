package lenacom.filer.component;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

public abstract class ButtonMenu extends XToggleButton implements ActionListener, PopupMenuListener {
    protected JPopupMenu menu;

    public ButtonMenu() {
        this.addActionListener(this);
        menu = new JPopupMenu();
        menu.addPopupMenuListener(this);
        this.setComponentPopupMenu(menu);

        menu.registerKeyboardAction(
            new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    menu.setVisible(false);
                }
            },
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    @Override
    public void setIcon(Icon icon) {
        super.setIcon(icon);
        Dimension size = this.getPreferredSize();
        size.width = size.height;
        this.setPreferredSize(size);
    }

    @Override
    public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
        getModel().setSelected(false);
    }

    @Override
    public void popupMenuCanceled(PopupMenuEvent e) {
        getModel().setSelected(false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (this.isSelected()) {
            if (menu.getComponentCount() == 0) {
                initMenuItems();
            }
            int y = this.getSize().height;
            menu.show(this, 0, y);
        }
    }

    protected abstract void initMenuItems();
}

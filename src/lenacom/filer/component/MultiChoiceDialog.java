package lenacom.filer.component;

import lenacom.filer.config.ResourceKey;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MultiChoiceDialog extends ButtonsDialog {
    private JButton[] buttons;
    private HtmlLabel lblMessage;

    public MultiChoiceDialog(Component owner, ResourceKey keyTitle, ResourceKey keyMessage, Action... actions) {
        super(owner, keyTitle);
        lblMessage = new HtmlLabel(keyMessage);
        this.setCenterComponent(lblMessage);
        addButtons(actions);
        registerKeyboardAction();
    }

    private void addButtons(Action... actions) {
        FocusListener focusListener = new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                JButton button = ((JButton) e.getSource());
                getRootPane().setDefaultButton(button);
            }
        };

        ActionListener disposeListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        };

        buttons = new JButton[actions.length];
        int i = 0;
        for (Action action: actions) {
            JButton button = new JButton(action);
            button.addFocusListener(focusListener);
            button.addActionListener(disposeListener);
            buttons[i++] = button;
        }
        this.addRightButtons(buttons);
    }
    
    private int getButtonIndex(JButton button) {
        for (int i = 1; i < buttons.length; i++) {
            if (buttons[i] == button) return i;
        }
        return 0;
    }

    private void registerKeyboardAction() {
        getRootPane().registerKeyboardAction(
            new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JButton button = ((JRootPane) e.getSource()).getDefaultButton();
                    int prevButtonIndex  = getButtonIndex(button) - 1;
                    if (prevButtonIndex < 0) prevButtonIndex = buttons.length - 1;
                    buttons[prevButtonIndex].requestFocusInWindow();
                }
            },
            KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW);

        getRootPane().registerKeyboardAction(
            new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JButton button = ((JRootPane) e.getSource()).getDefaultButton();
                    int nextButtonIndex  = getButtonIndex(button) + 1;
                    if (nextButtonIndex > buttons.length - 1) nextButtonIndex = 0;
                    buttons[nextButtonIndex].requestFocusInWindow();
                }
            },
            KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    @Override
    protected boolean onCancel() {
        //do nothing
        return true;
    }

    @Override
    public void pack() {
        super.pack();
        int diff = lblMessage.adjustHeight();
        this.setHeight(getHeight() + diff);
    }
}

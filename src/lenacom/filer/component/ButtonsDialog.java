package lenacom.filer.component;

import lenacom.filer.config.ResourceKey;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

public abstract class ButtonsDialog extends AbstractDialog {
    private JPanel pnlLeftButtons;
    private JPanel pnlRightButtons;

    public ButtonsDialog(Component owner, String keyTitle) {
        this(owner);
        super.setTitle(keyTitle);
    }

    public ButtonsDialog(Component owner, ResourceKey keyTitle) {
        this(owner);
        super.setTitle(keyTitle);
    }

    public ButtonsDialog(Component owner) {
        super(owner);
        registerActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        onEscape();
                    }
                },
                KeyEvent.VK_ESCAPE, 0);

        this.setLayout(new GridBagLayout());

        pnlLeftButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlRightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.NONE;
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        constraints.insets = new Insets(0, UIConstants.BORDER_GAP, UIConstants.BORDER_GAP, UIConstants.HORIZONTAL_GAP);
        this.add(pnlLeftButtons, constraints);

        constraints.gridx = 1;
        constraints.anchor = GridBagConstraints.EAST;
        constraints.insets = new Insets(0, 0, UIConstants.BORDER_GAP, UIConstants.BORDER_GAP);
        this.add(pnlRightButtons, constraints);
    }

    private void addButtons(JPanel pnl, AbstractButton... newButtons) {
        for (AbstractButton button: newButtons) {
            if (pnl.getComponentCount() > 0) {
                Component rigidArea = Box.createRigidArea(new Dimension(UIConstants.HORIZONTAL_GAP, 0));
                pnl.add(rigidArea);
            }
            pnl.add(button);
        }
    }

    public void addLeftButtons(AbstractButton... newButtons) {
        addButtons(pnlLeftButtons, newButtons);
    }

    public void addRightButtons(AbstractButton... newButtons) {
        addButtons(pnlRightButtons, newButtons);
    }

    protected void onEscape() {
        if (onCancel()) {
            dispose();
        }
    }

    public void setCenterComponent(JComponent component) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 2;
        constraints.anchor = GridBagConstraints.NORTH;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.insets = new Insets(UIConstants.BORDER_GAP, UIConstants.BORDER_GAP, UIConstants.BORDER_GAP, UIConstants.BORDER_GAP);
        this.add(component, constraints);
    }
}

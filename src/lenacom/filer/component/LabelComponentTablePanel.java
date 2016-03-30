package lenacom.filer.component;

import lenacom.filer.config.ResourceKey;

import javax.swing.*;
import java.awt.*;

public class LabelComponentTablePanel extends JPanel {
    private int countRows = 0;

    public LabelComponentTablePanel() {
        this.setLayout(new GridBagLayout());
    }

    public void addRow(String keyLabel, String keyValue) {
        addRow(keyLabel, XLabel.create(keyValue));
    }

    public void addRow(ResourceKey keyLabel, final JComponent component, String tooltip) {
        LabelComponent labelComponent = new LabelComponent(keyLabel, component, tooltip);
        addRow(labelComponent);
    }

    public void addRow(ResourceKey keyLabel, final JComponent component, ResourceKey keyTooltip) {
        LabelComponent labelComponent = new LabelComponent(keyLabel, component, keyTooltip);
        addRow(labelComponent);
    }

    public void addRow(String keyLabel, final JComponent component) {
        LabelComponent labelComponent = new LabelComponent(keyLabel, component);
        addRow(labelComponent);
    }

    private void addRow(LabelComponent labelComponent) {
        assert(SwingUtilities.isEventDispatchThread());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = countRows++;
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.insets = new Insets(countRows == 0? 0 : UIConstants.VERTICAL_GAP, 0, 0, 0);
        this.add(labelComponent.getLabel(), constraints);

        constraints.gridx = 1;
        constraints.weightx = 1.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(countRows == 0? 0 : UIConstants.VERTICAL_GAP, UIConstants.HORIZONTAL_GAP, 0, 0);
        this.add(labelComponent.getComponent(), constraints);
    }
}

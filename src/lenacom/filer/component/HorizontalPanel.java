package lenacom.filer.component;

import javax.swing.*;
import java.awt.*;

public class HorizontalPanel extends JPanel {
    public HorizontalPanel(JComponent... components) {
        this.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        for(int i = 0; i < components.length; i++) {
            constraints.gridx = i;
            constraints.gridy = 0;
            constraints.anchor = GridBagConstraints.WEST;
            constraints.fill = GridBagConstraints.NONE;
            constraints.weightx = i == components.length - 1 ? 1.0 : 0;
            constraints.insets = new Insets(0, i == 0? 0 : UIConstants.HORIZONTAL_GAP, 0, 0);
            this.add(components[i], constraints);
        }
    }
}

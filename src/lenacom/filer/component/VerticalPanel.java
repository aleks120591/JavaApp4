package lenacom.filer.component;

import javax.swing.*;
import java.awt.*;

public class VerticalPanel extends JPanel {
    public VerticalPanel(JComponent... components) {
        this.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        for(int i = 0; i < components.length; i++) {
            constraints.gridx = 0;
            constraints.gridy = i;
            constraints.anchor = GridBagConstraints.NORTH;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.weighty = i == components.length - 1 ? 1.0 : 0;
            constraints.insets = new Insets(i == 0? 0 : UIConstants.VERTICAL_GAP, 0, 0, 0);
            this.add(components[i], constraints);
        }
    }
}

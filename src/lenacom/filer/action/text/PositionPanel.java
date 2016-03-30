package lenacom.filer.action.text;

import javax.swing.*;
import java.awt.*;

class PositionPanel extends JPanel {
    private JLabel lblRow, lblColumn;

    PositionPanel() {
        this.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        this.lblRow = new JLabel();
        this.lblColumn = new JLabel();
        this.add(lblRow);
        this.add(new JLabel(":"));
        this.add(lblColumn);
    }

    void setPosition(int row, int column) {
        lblRow.setText(String.valueOf(row));
        lblColumn.setText(String.valueOf(column));
    }

}

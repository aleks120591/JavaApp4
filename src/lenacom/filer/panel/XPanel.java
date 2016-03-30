package lenacom.filer.panel;

import lenacom.filer.component.Refreshable;
import lenacom.filer.component.XSplitPane;

import javax.swing.*;
import java.awt.*;

public class XPanel extends JPanel implements Refreshable {
    private XSplitPane splitPane;
    private HeaderPanel headerPanel;
    private XTable table;
    private FooterPanel footerPanel;

    XPanel(XTableImpl table) {
        this.table = table;
        headerPanel = new HeaderPanel(this);

        splitPane = new XSplitPane(JSplitPane.VERTICAL_SPLIT,
            headerPanel, table.getWrapper()
        );

        footerPanel = new FooterPanel(table);

        this.setLayout(new BorderLayout());
        this.setDoubleBuffered(true);
        add(splitPane, BorderLayout.CENTER);
        add(footerPanel, BorderLayout.SOUTH);
    }

    public XTable getTable() {
        return table;
    }

    public void showGoMenu() {
        headerPanel.showGoMenu();
    }

    public void showDoMenu() {
        headerPanel.showDoMenu();
    }

    public void refresh() {
        table.refresh();
        headerPanel.refresh();
    }

    XSplitPane getSplitPane() {
        return splitPane;
    }
}

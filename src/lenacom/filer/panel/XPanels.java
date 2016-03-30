package lenacom.filer.panel;

import lenacom.filer.path.PathUtils;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public enum XPanels {
    INSTANCE;
    private XPanel LEFT, RIGHT;
    private XPanel activePanel;

    private class DirectoryExistenceChecker extends XTableAdapter {
        private final XTableImpl slave;

        DirectoryExistenceChecker(XTableImpl master, XTableImpl slave) {
            this.slave = slave;
            master.getModel().addListener(this);
        }

        @Override
        public void pathModified(Path path) {
            check();
        }

        @Override
        public void pathDeleted(Path path) {
            check();
        }

        private void check() {
            XTableContext context = slave.getContext();
            Path nativePath = context.getClosestNativePath();
            if (!PathUtils.existsFollowLink(nativePath)) slave.refresh();
        }
    }

    private XPanels() {
        init();
    }

    private void init() {
        XTableImpl leftTable = new XTableImpl(1);
        XTableImpl rightTable = new XTableImpl(2);

        LEFT = new XPanel(leftTable);
        RIGHT = new XPanel(rightTable);

        activePanel = LEFT;

        XPanel[] panels = new XPanel[]{LEFT, RIGHT};
        for (final XPanel panel: panels) {
            panel.getSplitPane().addPropertyChangeListener(
                    JSplitPane.LAST_DIVIDER_LOCATION_PROPERTY,
                    new PropertyChangeListener() {
                        @Override
                        public void propertyChange(PropertyChangeEvent event) {
                            //both headers with breadcrums must be of the same max height
                            final XPanel otherPanel = panel == LEFT ? RIGHT : LEFT;
                            otherPanel.getSplitPane().setDividerLocation(panel.getSplitPane().getDividerLocation());
                        }
                    }
            );
        }

        new DirectoryExistenceChecker(leftTable, rightTable);
        new DirectoryExistenceChecker(rightTable, leftTable);
    }

    public static void reload() {
        INSTANCE.init();
    }

    public static XPanel getLeftPanel() {
        return INSTANCE.LEFT;
    }

    public static XPanel getRightPanel() {
        return INSTANCE.RIGHT;
    }

    public static XPanel getActivePanel() {
        return INSTANCE.activePanel;
    }

    public static XPanel getPassivePanel() {
        return getOtherPanel(getActivePanel());
    }

    public static void changeActivePanel() {
        setActiveTable(getPassivePanel().getTable());
    }

    public static XPanel getOtherPanel(XPanel panel) {
        return panel == INSTANCE.LEFT? INSTANCE.RIGHT : INSTANCE.LEFT;
    }

    static void setActiveTable(XTable table) {
        assert(SwingUtilities.isEventDispatchThread());
        if (table == INSTANCE.activePanel.getTable()) return;
        INSTANCE.activePanel = table == INSTANCE.LEFT.getTable()? INSTANCE.LEFT : INSTANCE.RIGHT;
        XTableImpl passiveTable = (XTableImpl) getPassivePanel().getTable();
        XTableImpl activeTable = (XTableImpl) table;
        passiveTable.getTable().repaint();
        activeTable.getTable().repaint();
        activeTable.getTable().requestFocusInWindow();
    }

    static XTable getActiveTable() {
        return INSTANCE.activePanel.getTable();
    }
}

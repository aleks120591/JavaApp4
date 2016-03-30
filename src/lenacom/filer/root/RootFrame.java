package lenacom.filer.root;

import lenacom.filer.action.Actions;
import lenacom.filer.action.XAction;
import lenacom.filer.config.*;
import lenacom.filer.message.Confirmation;
import lenacom.filer.component.XSplitPane;
import lenacom.filer.menu.TopMenu;
import lenacom.filer.panel.XPanels;
import lenacom.filer.util.KeyActionsUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

public enum RootFrame {
    INSTANCE;
    private JFrame root;

    private RootFrame() {
        this.root = getRootFrame();
    }

    private JFrame getRootFrame() {
        JFrame root = new JFrame(Resources.getMessage("filer.title") + " 1.0");
        root.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        root.getContentPane().add(getRootPanel(), BorderLayout.CENTER);
        root.setJMenuBar(new TopMenu());
        root.pack();
        root.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        root.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exit();
            }
        });

        setSizeAndPosition(root);

        setIcons(root);
        ToolTipManager.sharedInstance().setDismissDelay(Constants.TOOLTIP_DISMISS_DELAY);

        return root;
    }

    private void setIcons(JFrame root) {
        java.util.List<Image> icons = new ArrayList<>();
        icons.add(XIcon.FILER16.getIcon().getImage());
        icons.add(XIcon.FILER32.getIcon().getImage());
        root.setIconImages(icons);
    }

    public static void show() {
        assert(SwingUtilities.isEventDispatchThread());
        INSTANCE.root.setVisible(true);
    }

    private JPanel getRootPanel() {
        JPanel panel = new JPanel(new BorderLayout(), true);

        XSplitPane splitPane = new XSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                XPanels.getLeftPanel(),
                XPanels.getRightPanel()
        );
        splitPane.setResizeWeight(0.5);
        panel.add(splitPane, BorderLayout.CENTER);

        //register actions after all children are added
        for (XAction action: Actions.getAllActions()) {
            KeyActionsUtils.registerAction(panel, action, action.getAccelerator(), action.getSecondAccelerator());
        }
        //in menus "Open" must not have the accelerator [Enter]
        KeyActionsUtils.registerAction(panel, Actions.getOpenAction(), KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));

        return panel;
    }

    public static void exit() {
        int count = FileOperationWorkers.count();
        if (count > 0 &&
                !Confirmation.confirm(new ResourceKey("confirm.exit.interrupt.file.operations", count))) {
            return;
        }
        ModelessDialogs.closeAll();
        saveSizeAndPosition(getRoot());
        System.exit(0);
    }

    private static void saveSizeAndPosition(JFrame root) {
        Dimension size = root.getSize();
        Configuration.setInteger(Configuration.WINDOW_WIDTH, size.width);
        Configuration.setInteger(Configuration.WINDOW_HEIGHT, size.height);
        Point point = root.getLocationOnScreen();
        Configuration.setInteger(Configuration.WINDOW_X, point.x);
        Configuration.setInteger(Configuration.WINDOW_Y, point.y);
    }

    private static void setSizeAndPosition(JFrame root) {
        Integer width = Configuration.getInteger(Configuration.WINDOW_WIDTH);
        Integer height = Configuration.getInteger(Configuration.WINDOW_HEIGHT);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension size;
        if (width != null && height != null) {
            int minSize = 200;
            if (width < minSize) width = minSize;
            if (height < minSize) height = minSize;
            size = new Dimension(width, height);
        } else {
            size = new Dimension(screenSize.width, screenSize.height);
            size.height = screenSize.height / 10 * 9;
        }
        root.setSize(size);

        Integer x = Configuration.getInteger(Configuration.WINDOW_X);
        Integer y = Configuration.getInteger(Configuration.WINDOW_Y);
        if (x != null && y != null &&
            x > 0 && x < screenSize.width &&
            y > 0 && y < screenSize.height
        ) {
            root.setLocation(x, y);
        } else {
            root.setLocationRelativeTo(null); //center the root on screen
        }
    }

    public static void restart() {
        assert(SwingUtilities.isEventDispatchThread());
        if (FileOperationWorkers.count() > 0) return;
        ModelessDialogs.closeAll();
        INSTANCE.root.dispose();
        INSTANCE.root = INSTANCE.getRootFrame();
        INSTANCE.root.setVisible(true);
    }

    public static JFrame getRoot() {
        assert(SwingUtilities.isEventDispatchThread());
        return INSTANCE == null? null : INSTANCE.root;
    }

}
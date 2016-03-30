package lenacom.filer.message;

import lenacom.filer.config.ResourceKey;
import lenacom.filer.config.Resources;
import lenacom.filer.panel.XPanels;
import lenacom.filer.root.RootFrame;

import javax.swing.*;
import java.awt.*;

public class Errors {

    public static void showError(Throwable t) {
        Component owner = RootFrame.getRoot();
        showError(owner, t);
    }

    public static void showError(Component parent, Throwable t) {
        t.printStackTrace();
        String message = t.getMessage();
        if (t instanceof java.nio.file.AccessDeniedException) {
            message = Resources.getMessage(new ResourceKey("error.access.denied", message));
        }
        JOptionPane.showMessageDialog(parent, message,
                Resources.getMessage("error.title"),
                JOptionPane.ERROR_MESSAGE);
    }
}

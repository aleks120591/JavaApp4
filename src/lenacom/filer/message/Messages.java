package lenacom.filer.message;

import lenacom.filer.config.ResourceKey;
import lenacom.filer.config.Resources;
import lenacom.filer.root.RootFrame;

import javax.swing.*;
import java.awt.*;

public class Messages {

    public static void showMessage(String key, Object... params) {
        showMessage(RootFrame.getRoot(), key, params);
    }

    public static void showMessage(Component parent, String key, Object... params) {
        String message = Resources.getMessage(new ResourceKey(key, params));
        JOptionPane.showMessageDialog(parent, message,
                Resources.getMessage("info.title"),
                JOptionPane.INFORMATION_MESSAGE);
    }
}

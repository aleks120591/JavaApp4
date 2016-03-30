package lenacom.filer.root;

import lenacom.filer.component.Closable;
import lenacom.filer.component.Refreshable;

import javax.swing.*;
import java.util.HashSet;

public class ModelessDialogs {
    private static HashSet<JDialog> dialogs = new HashSet<>();

    public static void add(JDialog dialog) {
        assert(SwingUtilities.isEventDispatchThread());
        dialogs.add(dialog);
    }

    public static void remove(JDialog dialog) {
        assert(SwingUtilities.isEventDispatchThread());
        dialogs.remove(dialog);
    }

    public static void refreshAll() {
        assert(SwingUtilities.isEventDispatchThread());
        for (JDialog dialog : dialogs) {
            if (dialog instanceof Refreshable) {
                ((Refreshable) dialog).refresh();
            }
        }
    }

    public static void closeAll() {
        assert(SwingUtilities.isEventDispatchThread());
        for (JDialog dialog : dialogs) {
            if (dialog instanceof Closable) {
                ((Closable) dialog).close();
            }
        }
    }
}

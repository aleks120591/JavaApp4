package lenacom.filer.config;

import lenacom.filer.component.ListDialog;
import lenacom.filer.message.Errors;
import lenacom.filer.panel.XPanels;
import lenacom.filer.root.RootFrame;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.util.ArrayList;

public class LookAndFeel {

    public static void chooseLookAndFeel() {
        new LookAndFeelDialog();
    }

    private final static class LookAndFeelDialog extends ListDialog<Item> {
        private Item currentLookAndFeel;
        private Item selectedLookAndFeel;

        LookAndFeelDialog() {
            super(RootFrame.getRoot(), "dlg.look.and.feel.title");

            java.util.List<Item> data = new ArrayList<>();
            UIManager.getCrossPlatformLookAndFeelClassName();
            String selectedName = UIManager.getLookAndFeel().getName();
            for (UIManager.LookAndFeelInfo info: UIManager.getInstalledLookAndFeels()) {
                Item item = new Item(info);
                if (info.getName().equals(selectedName)) currentLookAndFeel = item;
                data.add(item);
            }

            setList(data.toArray(new Item[data.size()]), currentLookAndFeel);
            setVisibleRelativeToParent();
        }

        @Override
        protected boolean onOk() {
            String className = getSelectedValue().getClassName();
            Configuration.setString(Configuration.LOOK_AND_FEEL_CLASS_NAME_KEY, className);
            boolean result =  setLookAndFeel(getSelectedValue());
            XPanels.getLeftPanel().refresh();
            XPanels.getRightPanel().refresh();
            return result;
        }

        protected boolean onCancel() {
            if (selectedLookAndFeel != currentLookAndFeel) {
                setLookAndFeel(currentLookAndFeel);
            }
            return true;
        }

        @Override
        public void valueChanged(ListSelectionEvent e) {
            selectedLookAndFeel = getSelectedValue();
            setLookAndFeel(selectedLookAndFeel);
            this.revalidate();
            this.repaint();
        }

        private boolean setLookAndFeel(Item item) {
            String className = item.getClassName();
            try {
                UIManager.setLookAndFeel(className);
                SwingUtilities.updateComponentTreeUI(RootFrame.getRoot());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
                Errors.showError(LookAndFeelDialog.this, e);
                return false;
            }
            return true;
        }
    }

    public static String getLookAndFeelClassName() {
        return Configuration.getString(Configuration.LOOK_AND_FEEL_CLASS_NAME_KEY);
    }

    private static class Item {
        private UIManager.LookAndFeelInfo info;

        Item(UIManager.LookAndFeelInfo info) {
            this.info = info;
        }
        
        String getClassName() {
            return info.getClassName();
        }

        public String toString() {
            return info.getName();
        }
    }
}

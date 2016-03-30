package lenacom.filer.menu;

import lenacom.filer.action.Actions;
import lenacom.filer.action.XAction;
import lenacom.filer.component.XMenu;

import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

public class TopMenu extends JMenuBar {

    public TopMenu() {
        add(createWorkspaceMenu());
        add(createConfigurationMenu());
    }

    private JMenu createWorkspaceMenu() {
        JMenu menu = new XMenu("menu.top.work");
        menu.add(Actions.getChangeActivePanelAction());
        addMenuItem(menu, Actions.getDoAction());
        addMenuItem(menu, Actions.getGoAction());
        menu.add(Actions.getContextMenuAction());
        menu.add(new JSeparator());
        addMenuItem(menu, Actions.getQuitAction());
        return menu;
    }

    private JMenu createConfigurationMenu() {
        JMenu menu = new XMenu("menu.top.config");

        final JCheckBoxMenuItem miHiddenFiles = new JCheckBoxMenuItem(Actions.getHiddenFilesAction());
        menu.add(miHiddenFiles);

        final JCheckBoxMenuItem miShowAttributes = new JCheckBoxMenuItem(Actions.getShowAttributesAction());
        menu.add(miShowAttributes);

        final JCheckBoxMenuItem miEyesFriendlyMode = new JCheckBoxMenuItem(Actions.getEyesFriendlyModeAction());
        menu.add(miEyesFriendlyMode);

        final JCheckBoxMenuItem miExtensionColors = new JCheckBoxMenuItem(Actions.getExtensionColorsAction());
        menu.add(miExtensionColors);

        menu.add(new JSeparator());
        menu.add(Actions.getLookAndFeelAction());
        menu.add(Actions.getFontAction());
        menu.add(Actions.getLanguageAction());
        menu.add(new JSeparator());
        menu.add(Actions.getRestoreDefaultsAction());

        menu.addMenuListener(new MenuListener() {
            @Override
            public void menuSelected(MenuEvent e) {
                miHiddenFiles.setSelected(Actions.getHiddenFilesAction().isSelected());
                miShowAttributes.setSelected(Actions.getShowAttributesAction().isSelected());
                miEyesFriendlyMode.setSelected(Actions.getEyesFriendlyModeAction().isSelected());
                miExtensionColors.setSelected(Actions.getExtensionColorsAction().isSelected());
            }

            @Override
            public void menuDeselected(MenuEvent e) {}

            @Override
            public void menuCanceled(MenuEvent e) {}
        });

        return menu;
    }

    private void addMenuItem(JMenu menu, XAction action) {
        JMenuItem item = new JMenuItem(action);
        item.setIcon(action.getIcon());
        menu.add(item);
    }
}
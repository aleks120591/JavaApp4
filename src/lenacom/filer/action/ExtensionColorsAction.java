package lenacom.filer.action;

import lenacom.filer.root.ModelessDialogs;
import lenacom.filer.config.Colors;
import lenacom.filer.panel.XPanels;

class ExtensionColorsAction extends BooleanAction {

    public ExtensionColorsAction(String key, boolean withEllipsis) {
        super(key, withEllipsis);
    }

    @Override
    public boolean isSelected() {
        return Colors.getExtensionColors();
    }

    @Override
    public void act() {
        Colors.setExtensionColors(!Colors.getExtensionColors());
        XPanels.getLeftPanel().getTable().refresh();
        XPanels.getRightPanel().getTable().refresh();
        ModelessDialogs.refreshAll();
    }
}

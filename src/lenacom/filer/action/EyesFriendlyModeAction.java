package lenacom.filer.action;

import lenacom.filer.root.ModelessDialogs;
import lenacom.filer.config.Colors;
import lenacom.filer.panel.XPanels;

class EyesFriendlyModeAction extends BooleanAction {

    public EyesFriendlyModeAction(String key, boolean withEllipsis) {
        super(key, withEllipsis);
    }

    @Override
    public boolean isSelected() {
        return Colors.getEyesFriendlyMode();
    }

    @Override
    public void act() {
        Colors.setEyesFriendlyMode(!Colors.getEyesFriendlyMode());
        XPanels.getLeftPanel().refresh();
        XPanels.getRightPanel().refresh();
        ModelessDialogs.refreshAll();
    }
}
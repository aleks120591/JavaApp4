package lenacom.filer.action;

import lenacom.filer.config.Settings;
import lenacom.filer.panel.XPanels;

class ShowAttributesAction extends BooleanAction {

    public ShowAttributesAction(String key, boolean withEllipsis) {
        super(key, withEllipsis);
    }

    @Override
    public boolean isSelected() {
        return Settings.isShowAttributes();
    }

    @Override
    public void act() {
        Settings.toggleShowAttribute();
        XPanels.getLeftPanel().getTable().refresh();
        XPanels.getRightPanel().getTable().refresh();
    }
}

package lenacom.filer.action;

import lenacom.filer.config.Configuration;
import lenacom.filer.panel.XPanels;

class HiddenFilesAction extends BooleanAction {

    public HiddenFilesAction(String key, boolean withEllipsis) {
        super(key, withEllipsis);
    }

    @Override
    public boolean isSelected() {
        return Configuration.getBoolean(Configuration.SHOW_HIDDEN_FILES, Boolean.FALSE);
    }

    @Override
    public void act() {
        Boolean showHiddenFiles = Configuration.getBoolean(Configuration.SHOW_HIDDEN_FILES, Boolean.FALSE);
        showHiddenFiles = !showHiddenFiles;
        Configuration.setBoolean(Configuration.SHOW_HIDDEN_FILES, showHiddenFiles);
        XPanels.getLeftPanel().getTable().refresh();
        XPanels.getRightPanel().getTable().refresh();
    }
}

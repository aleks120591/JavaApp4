package lenacom.filer.action;

import lenacom.filer.config.XIcon;
import lenacom.filer.panel.XPanel;
import lenacom.filer.panel.XPanels;

import java.nio.file.Path;

class OtherPanelDirectoryAction extends XAction {
    public OtherPanelDirectoryAction(String key, boolean withEllipsis) {
        super(key, withEllipsis);
        this.setIcon(XIcon.PANELS.getIcon());
    }

    @Override
    public void act() {
        XPanel passivePanel = XPanels.getOtherPanel(getPanel());
        Path workingDirectory = passivePanel.getTable().getContext().getDirectory();
        getTable().setContextDirectory(workingDirectory);
    }
}

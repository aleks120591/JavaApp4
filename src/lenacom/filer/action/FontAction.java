package lenacom.filer.action;

import lenacom.filer.root.ModelessDialogs;
import lenacom.filer.component.FontDialog;
import lenacom.filer.config.Fonts;
import lenacom.filer.panel.XPanels;
import lenacom.filer.root.RootFrame;

import java.awt.*;

class FontAction extends XAction {

    FontAction(String key, boolean withEllipsis) {
        super(key, withEllipsis);
    }

    @Override
    public void act() {
        new FontDialog(RootFrame.getRoot(), Fonts.getFont()) {
            @Override
            protected void applyFont(Font font) {
                Fonts.setFont(font);
                XPanels.getLeftPanel().refresh();
                XPanels.getRightPanel().refresh();
                ModelessDialogs.refreshAll();
            }
        };
    }
}

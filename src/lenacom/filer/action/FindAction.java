package lenacom.filer.action;

import lenacom.filer.action.find.FindDialog;
import lenacom.filer.config.XIcon;
import lenacom.filer.panel.XPanels;

public class FindAction extends XAction {

    public FindAction(String key, boolean withEllipsis) {
        super(key, withEllipsis);
        this.setIcon(XIcon.FIND.getIcon());
    }

    @Override
    public void act() {
        new FindDialog(getTable().getWrapper(), getTable().getContext(),
                XPanels.getOtherPanel(getPanel()).getTable());
    }

}

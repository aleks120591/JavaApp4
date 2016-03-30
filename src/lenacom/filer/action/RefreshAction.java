package lenacom.filer.action;

import lenacom.filer.config.XIcon;

class RefreshAction extends XAction {

    RefreshAction(String key, boolean withEllipsis) {
        super(key, withEllipsis);
        setIcon(XIcon.REFRESH.getIcon());
    }

    @Override
    public void act() {
        getTable().refresh();
    }
}

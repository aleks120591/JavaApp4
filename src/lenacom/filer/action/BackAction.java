package lenacom.filer.action;

import lenacom.filer.config.XIcon;

class BackAction extends XAction {

    BackAction(String key, boolean withEllipsis) {
        super(key, withEllipsis);
        this.setIcon(XIcon.BACK.getIcon());
    }

    @Override
    public void act() {
        getTable().getStack().back();
    }

    @Override
    public boolean isEnabled() {
        return getTable().getStack().hasBackItem();
    }
}

package lenacom.filer.action;

import lenacom.filer.config.XIcon;

class ForwardAction extends XAction {

    public ForwardAction(String key, boolean withEllipsis) {
        super(key, withEllipsis);
        this.setIcon(XIcon.FORWARD.getIcon());
    }

    @Override
    public void act() {
        getTable().getStack().forward();
    }

    @Override
    public boolean isEnabled() {
        return getTable().getStack().hasForwardItem();
    }
}

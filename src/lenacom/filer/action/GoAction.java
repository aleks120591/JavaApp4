package lenacom.filer.action;

import lenacom.filer.config.XIcon;

class GoAction extends XAction{

    GoAction(String key, boolean withEllipsis) {
        super(key, withEllipsis);
        this.setIcon(XIcon.HAND.getIcon());
    }

    @Override
    public void act() {
        getPanel().showGoMenu();
    }
}

package lenacom.filer.action;

import lenacom.filer.config.XIcon;

class DoAction extends XAction {

    DoAction(String key, boolean withEllipsis) {
        super(key, withEllipsis);
        this.setIcon(XIcon.HAMMER.getIcon());
    }

    @Override
    public void act() {
        getPanel().showDoMenu();
    }
}

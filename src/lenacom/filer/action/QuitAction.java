package lenacom.filer.action;

import lenacom.filer.root.RootFrame;

class QuitAction extends XAction {

    public QuitAction(String key, boolean withEllipsis) {
        super(key, withEllipsis);
    }

    @Override
    public void act() {
        RootFrame.exit();
    }
}

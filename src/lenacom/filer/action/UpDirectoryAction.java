package lenacom.filer.action;

import lenacom.filer.config.XIcon;
import lenacom.filer.panel.PathRow;

class UpDirectoryAction extends XAction {

    public UpDirectoryAction(String key, boolean withEllipsis) {
        super(key, withEllipsis);
        this.setIcon(XIcon.UP.getIcon());
    }

    @Override
    public boolean isEnabled() {
        return getTable().getParentRow() != null;
    }

    @Override
    public void act() {
        PathRow parentRow = getTable().getParentRow();
        if (parentRow != null) {
            getTable().setContextDirectory(parentRow.getPath());
        }
    }
}

package lenacom.filer.action;

public abstract class BooleanAction extends XAction {

    protected BooleanAction(String key, boolean withEllipsis) {
        super(key, withEllipsis);
    }

    public abstract boolean isSelected();
}

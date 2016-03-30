package lenacom.filer.action;

import lenacom.filer.action.text.FileDialog;
import lenacom.filer.message.Errors;
import lenacom.filer.panel.XTableContext;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class ViewAction extends XAction implements ContextAction {
    private static final Set<SelectionType> types;

    static {
        types = new HashSet<>();
        types.add(SelectionType.FILE);
        types.add(SelectionType.SYMLINK_FILE);
    }

    @Override
    public Set<SelectionType> getSelectionTypes() {
        return types;
    }

    public ViewAction(String key, boolean withEllipsis) {
        super(key, withEllipsis);
    }

    @Override
    public boolean isEnabled() {
        SelectionType type = SelectionType.getSelectionType(getTable());
        return types.contains(type);
    }

    @Override
    public void act() {
        view(getTable().getWrapper(), getTable().getContext(),
                getTable().getFocusedRow().getPath());
    }

    public void view(Component parent, XTableContext context, Path path) {
        try {
            new FileDialog(parent, context, path, false);
        } catch (IOException e) {
            Errors.showError(e);
        }
    }

    public void view(Component parent, XTableContext context, Path path, String findText) {
        try {
            new FileDialog(parent, context, path, findText, false);
        } catch (IOException e) {
            Errors.showError(e);
        }
    }

}

package lenacom.filer.action;

import lenacom.filer.action.text.FileDialog;
import lenacom.filer.config.XIcon;
import lenacom.filer.message.Errors;
import lenacom.filer.panel.XTableContext;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class EditAction extends XAction implements ContextAction {
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

    public EditAction(String key, boolean withEllipsis) {
        super(key, withEllipsis);
        this.setIcon(XIcon.EDIT.getIcon());
    }

    @Override
    public boolean isEnabled() {
        SelectionType type = SelectionType.getSelectionType(getTable());
        return types.contains(type);
    }

    @Override
    public void act() {
        edit(getTable().getWrapper(), getTable().getContext(),
                getTable().getFocusedRow().getPath());
    }

    public void edit(Path path) {
        edit(getTable().getWrapper(), getTable().getContext(), path);
    }

    public void edit(Component parent, XTableContext context, Path path) {
        try {
            new FileDialog(parent, context, path, true);
        } catch (IOException e) {
            Errors.showError(e);
        }
    }

    public void edit(Component parent, XTableContext context, Path path, String findText) {
        try {
            new FileDialog(parent, context, path, findText, true);
        } catch (IOException e) {
            Errors.showError(e);
        }
    }

}

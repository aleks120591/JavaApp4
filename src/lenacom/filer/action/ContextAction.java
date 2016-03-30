package lenacom.filer.action;

import java.util.Set;

public interface ContextAction {
    Set<SelectionType> getSelectionTypes();
    String getName();
}

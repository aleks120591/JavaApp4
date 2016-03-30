package lenacom.filer.config;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public enum History {
    FIND_TEXT_HISTORY,
    REPLACE_HISTORY,
    FIND_PATH_NAME_HISTORY;

    private final static int HISTORY_SIZE = 20;
    private List<String> history = new ArrayList<>(HISTORY_SIZE);

    private History() {}

    public void addItemOnTop(String value) {
        assert(SwingUtilities.isEventDispatchThread());
        if (value == null || value.isEmpty()) return;
        //add on the top, keep a unique list
        history.remove(value);
        if (history.size() == HISTORY_SIZE) {
            history.remove(HISTORY_SIZE - 1);
        }
        history.add(0, value);
    }

    public int getSize() {
        return history.size();
    }

    public String getItemAt(int index) {
        return history.get(index);
    }

    public void clear() {
        history.clear();
    }

}

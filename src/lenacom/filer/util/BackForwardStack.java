package lenacom.filer.util;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public abstract class BackForwardStack<E> {
    private List<E> stack = new ArrayList<>();
    private int index = -1; //index point to the current item

    public void addItem(E item) {
        assert(SwingUtilities.isEventDispatchThread());
        int i;
        while ((i = stack.size() - 1) > index) stack.remove(i);
        //don't add the same item twice
        if (stack.size() > 0 && item.equals(stack.get(stack.size() - 1))) return;
        stack.add(item);
        index = stack.size() - 1;
    }

    public boolean hasBackItem() {
        return index > 0;
    }

    public boolean hasForwardItem() {
        return index < stack.size() - 1;
    }

    public void back() {
        assert(SwingUtilities.isEventDispatchThread());
        if (hasBackItem()) {
            index--;
            activate(stack.get(index));
        }
    }

    public void forward() {
        assert(SwingUtilities.isEventDispatchThread());
        if (hasForwardItem()) {
            index++;
            activate(stack.get(index));
        }
    }

    public abstract void activate(E item);
}

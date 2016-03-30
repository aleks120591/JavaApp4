package lenacom.filer.util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.util.WeakHashMap;

public class KeyActionsUtils {
    private static final String FILER = "Filer ";
    private static WeakHashMap<BasicAction, ContainerListener> listeners = new WeakHashMap<>();

    public static void registerActionListener(JComponent component, final ActionListener listener, final KeyStroke... accelerators) {
        String name = FILER + Math.random();
        BasicAction action = new BasicAction(name) {
            @Override
            public void actionPerformed(ActionEvent e) {
                listener.actionPerformed(e);
            }
        };
        registerAction(component, action, accelerators);
    }

    public static void registerButton(JComponent component, final AbstractButton button, final KeyStroke... accelerators) {
        String name = FILER + Math.random();
        BasicAction action = new BasicAction(name) {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (button.isEnabled()) button.doClick();
            }
        };
        registerAction(component, action, accelerators);
    }

    public static void registerAction(JComponent component, final BasicAction action, final KeyStroke... accelerators) {
        assert(SwingUtilities.isEventDispatchThread());
        String key = FILER + removeEllipsis(action.getName());

        ContainerListener listener = listeners.get(action);
        if (listener == null) {
            listener = new ContainerListener() {
                @Override
                public void componentAdded(ContainerEvent e) {
                    if (e.getChild() instanceof JComponent) {
                        registerAction((JComponent) e.getChild(), action, accelerators);
                    }
                }

                @Override
                public void componentRemoved(ContainerEvent e) {
                    //do nothing
                }
            };
            listeners.put(action, listener);
        }

        component.addContainerListener(listener);

        InputMap inputMap = component.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap actionMap = component.getActionMap();
        actionMap.put(key, action);

        for (KeyStroke accelerator: accelerators) {
            inputMap.put(accelerator, key);
        }
        for (Component child :component.getComponents()) {
            if (child instanceof JComponent) {
                registerAction((JComponent) child, action, accelerators);
            }
        }
    }

    private static String removeEllipsis(String name) {
        if (name.endsWith("...")) name = name.substring(0, name.length() - 3);
        return name;
    }
}

package lenacom.filer.path;

import lenacom.filer.message.Errors;

import javax.swing.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public enum Roots {
    INSTANCE;

    public static interface RootsListener {
        void rootsChanged(List<Path> roots);
    }

    private List<Path> roots;
    private List<RootsListener> listeners;

    private Roots() {
        roots = PathUtils.getRoots();
        listeners = new ArrayList<>();
        new Worker().execute();
    }

    public static List<Path> getRoots() {
         return INSTANCE.roots;
    }

    public static void addListener(RootsListener listener) {
        INSTANCE.listeners.add(listener);
    }

    public static void removeListener(RootsListener listener) {
        INSTANCE.listeners.remove(listener);
    }

    private static final class Worker extends SwingWorker<Void, List<Path>> {

        @Override
        protected Void doInBackground() {
            try {
                while(INSTANCE.roots != null) { //eternal cycle
                    Thread.sleep(1000); //each 1 sec
                    List<Path> newRoots = PathUtils.getRoots();
                    if (INSTANCE.roots.size() != newRoots.size() ||
                        /*size is equal but*/ !INSTANCE.roots.containsAll(newRoots)) {
                        INSTANCE.roots = newRoots;
                        publish(INSTANCE.roots);
                    }
                }
            } catch(Exception e) {
                Errors.showError(e);
            }
            return null; //never reached
        }

        @Override
        protected void process(List<List<Path>> chunks) {
            for (RootsListener listener: INSTANCE.listeners) {
                listener.rootsChanged(INSTANCE.roots);
            }
        }
    }
}

package lenacom.filer.root;

import javax.swing.*;
import java.util.HashSet;
import java.util.Set;

public class FileOperationWorkers {
    private static Set<SwingWorker> removeWorkers = new HashSet<>();
    private static Set<SwingWorker> workers = new HashSet<>();

    synchronized public static void add(SwingWorker worker) {
        clear();
        workers.add(worker);
    }

    synchronized private static void clear() {
        //use removeWorkers to avoid ConcurrentModificationException
        removeWorkers.clear();
        for (SwingWorker worker : workers) {
            if (worker.isCancelled() || worker.isDone()) {
                removeWorkers.add(worker);
            }
        }
        workers.removeAll(removeWorkers);
    }

    synchronized static public int count() {
        clear();
        return workers.size();
    }
}

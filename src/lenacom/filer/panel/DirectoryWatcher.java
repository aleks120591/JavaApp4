package lenacom.filer.panel;

import lenacom.filer.message.Errors;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;

class DirectoryWatcher extends SwingWorker<Void, DirectoryWatcher.Event>{
    private XTableModel model;
    private WatchService watcher;

    private enum EventType {REFRESH, INSERT, DELETE, UPDATE}
    protected class Event {
        EventType type;
        String name;
    }

    DirectoryWatcher(XTableModel model) {
        this.model = model;
        try {
            watcher = FileSystems.getDefault().newWatchService();
            model.getContextDirectory().getDirectory().register(watcher,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE,
                    StandardWatchEventKinds.ENTRY_MODIFY);
        } catch (IOException x) {
            x.printStackTrace();
        }
    }

    @Override
    protected void process(List<Event> events) {
        assert(SwingUtilities.isEventDispatchThread());
        for (Event event : events) {
            Path path = model.getContextDirectory().getDirectory().resolve(event.name);
            switch(event.type) {
                case REFRESH:
                    model.refresh();
                    return; //don't process subsequent events!
                case INSERT:
                    model.insert(path);
                    break;
                case DELETE:
                    model.delete(path);
                    break;
                case UPDATE:
                    model.update(path);
                    break;
            }
        }
    }

    @Override
    protected Void doInBackground() {
        try {
            tryToDoInBackground();
        } catch (Exception e) {
            Errors.showError(e);
        }
        return null;
    }

    private void tryToDoInBackground() throws Exception {
        while (!isCancelled()) {
            // wait for key to be signaled
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException x) {
                return;
            }

            for (WatchEvent<?> watchEvent: key.pollEvents()) {
                WatchEvent.Kind<?> kind = watchEvent.kind();

                // This key is registered only
                // for ENTRY_CREATE events,
                // but an OVERFLOW event can
                // occur regardless if events
                // are lost or discarded.
                if (kind == StandardWatchEventKinds.OVERFLOW) {
                    continue;
                }

                // The filename is the
                // context of the event.
                Event event = new Event();
                event.name = watchEvent.context().toString();
                if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                    event.type = EventType.INSERT;
                } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                    event.type = EventType.DELETE;
                } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                    event.type = EventType.UPDATE;
                }
                publish(event);
            }

            // Reset the key -- this step is critical if you want to
            // receive further watch events. If the key is no longer valid,
            // the directory is inaccessible so exit the loop.
            boolean valid = key.reset();
            if (!valid) {
                Event event = new Event();
                event.name = "";
                event.type = EventType.REFRESH;
                publish(event);
                break;
            }
        }

        try {
            watcher.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
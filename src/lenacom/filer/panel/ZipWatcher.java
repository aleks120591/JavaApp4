package lenacom.filer.panel;

import lenacom.filer.message.Errors;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;

class ZipWatcher extends SwingWorker<Void, String> {
    private XTableModel model;
    private WatchService watcher;
    private String zipFileName;

    ZipWatcher(XTableModel model, Path zip) {
        this.model = model;
        try {
            watcher = FileSystems.getDefault().newWatchService();
            if (Files.isSymbolicLink(zip)) zip = Files.readSymbolicLink(zip);
            zipFileName = zip.getFileName().toString();
            zip.getParent().register(watcher,
                StandardWatchEventKinds.ENTRY_DELETE, //when zip is modified it is rewritten
                StandardWatchEventKinds.ENTRY_CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void process(List<String> data) {
        assert(SwingUtilities.isEventDispatchThread());
        model.refresh();
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
        outer:
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
                if (watchEvent.context().toString().equals(zipFileName)) {
                    publish(zipFileName);
                    break outer;
                }
            }

            // Reset the key -- this step is critical if you want to
            // receive further watch events. If the key is no longer valid,
            // the directory is inaccessible so exit the loop.
            boolean valid = key.reset();
            if (!valid) {
                publish(zipFileName);
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

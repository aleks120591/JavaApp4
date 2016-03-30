package lenacom.filer.zip;

import lenacom.filer.config.ResourceKey;
import lenacom.filer.message.Confirmation;
import lenacom.filer.message.Errors;
import lenacom.filer.path.PathUtils;
import lenacom.filer.root.RootFrame;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;

class TempZipFileWatcher extends SwingWorker<Void, String> {
    private WatchService watcher;
    private Path tmpNativePath;
    private Path originalZipPath;
    private String tmpNativeName;
    private boolean updateZipImmediately = true;
    private volatile boolean changed = false;

    TempZipFileWatcher(Path tmpNativePath, Path originalZipPath, boolean updateZipImmediately) {
        this.tmpNativePath = tmpNativePath;
        this.originalZipPath = originalZipPath;
        tmpNativeName = tmpNativePath.getFileName().toString();
        this.updateZipImmediately = updateZipImmediately;
        try {
            watcher = FileSystems.getDefault().newWatchService();
            tmpNativePath.getParent().register(watcher,
                StandardWatchEventKinds.ENTRY_MODIFY,
                StandardWatchEventKinds.ENTRY_CREATE, //when zip is modified it is rewritten
                StandardWatchEventKinds.ENTRY_DELETE);
        } catch (IOException x) {
            x.printStackTrace();
        }
    }

    @Override
    protected void process(List<String> data) {
        assert(SwingUtilities.isEventDispatchThread());
        changed = true;
        if (updateZipImmediately) updateZipIfChanged();
    }

    void updateZip() {
        if (!PathUtils.existsNoFollowLink(tmpNativePath)) {
            this.cancel(true);
            return;
        }

        Path parentZip = PathUtils.getClosestExistentParent(originalZipPath);
        if (!PathUtils.existsNoFollowLink(parentZip)) {
            this.cancel(true);
            return;
        }

        if (Confirmation.confirm(new ResourceKey("confirm.zip.repack.changed.file", originalZipPath.getFileName(), parentZip))
                ) {
            try {
                ZipWorker zipWorker = ZipWorker.create(parentZip);
                zipWorker.replaceTmpFile(originalZipPath, tmpNativePath, false);
            } catch (IOException e) {
                Errors.showError(e);
            }
            //we do not delete the tmp file here, it's being packed into zip in a separate thread
        }
    }

    void updateZipIfChanged() {
        if (!changed) return;
        updateZip();
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
                if (watchEvent.context().toString().equals(tmpNativeName)) {
                    publish(tmpNativeName);
                }
            }

            // Reset the key -- this step is critical if you want to
            // receive further watch events. If the key is no longer valid,
            // the directory is inaccessible so exit the loop.
            boolean valid = key.reset();
            if (!valid) {
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

package lenacom.filer.zip;

import java.nio.file.Path;

public class ExtractedTmpFile {
    private Path tmpFile;
    private Path originalFile;
    private TempZipFileWatcher watcher;

    ExtractedTmpFile(Path originalFile, Path tmpFile) {
        this.tmpFile = tmpFile;
        this.originalFile = originalFile;
    }

    public Path getFile() {
        return tmpFile;
    }

    public void watchChanges() {
        watchChanges(false);
    }

    public void watchChangesAndUpdateZipImmediately() {
        watchChanges(true);
    }

    private void watchChanges(boolean updateZipImmediately) {
        if (tmpFile != null && originalFile != null) {
            watcher = new TempZipFileWatcher(tmpFile, originalFile, updateZipImmediately);
            watcher.execute();
        }
    }

    public void updateZip() {
        if (watcher != null) watcher.updateZip();
    }

    public void updateZipIfChanged() {
        if (watcher != null) watcher.updateZipIfChanged();
    }

}

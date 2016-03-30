package lenacom.filer.panel;

import lenacom.filer.path.processor.NativePathProcessor;
import lenacom.filer.path.processor.PathProcessor;
import lenacom.filer.zip.ZipWorker;

import java.nio.file.Path;

public class XTableContext {
    private ZipWorker zipWorker;
    private Path directory;

    public XTableContext(Path directory, ZipWorker zipWorker) {
        this.directory = directory;
        this.zipWorker = zipWorker;
    }

    public ZipWorker getZipWorker() {
        return zipWorker;
    }

    public Path getDirectory() {
        return directory;
    }

    public boolean isZip() {
        return zipWorker != null;
    }

    public Path getClosestNativePath() {
        if (zipWorker != null) return zipWorker.getZip();
        return directory;
    }

    public PathProcessor getPathProcessor() {
        if (zipWorker != null) return zipWorker.getPathProcessor();
        return NativePathProcessor.getInstance();
    }
}

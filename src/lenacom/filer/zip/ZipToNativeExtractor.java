package lenacom.filer.zip;

import lenacom.filer.config.ResourceKey;
import lenacom.filer.root.FileOperationWorkers;
import lenacom.filer.message.Errors;
import lenacom.filer.path.PathUtils;
import lenacom.filer.path.processor.NativePathProcessor;
import lenacom.filer.path.processor.OverwriteProcessor;
import lenacom.filer.progress.ExtendedPathProgress;
import lenacom.filer.progress.PublishedPath;
import lenacom.filer.root.RootFrame;

import javax.swing.*;
import java.io.BufferedOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

final class ZipToNativeExtractor extends SwingWorker<Void, PublishedPath> {
    private ZipModel model;
    private Path targetDirectory;
    private IncludePathFilter filter;
    private ZipInputStream zis;
    private ZipFile zipFile;
    private Path sourceDirectory = null; //can be null = zip root
    private boolean deleteExtractedPaths = false;
    private ExtendedPathProgress progress;
    private ZipExtractorOverwriteProcessor overwriteProcessor;
    private Path tmpZip;

    static void copyAll(ZipModel model, Path targetDirectory) {
        ZipToNativeExtractor extractor = new ZipToNativeExtractor(model, null, targetDirectory, false);
        extractor.execute();
    }

    static void copy(ZipModel model, Path[] source, Path targetDirectory) {
        process(model, source, targetDirectory, false);
    }

    static void move(ZipModel model, Path[] source, Path targetDirectory) {
        process(model, source, targetDirectory, true);
    }

    private static void process(ZipModel model, Path[] source, Path targetDirectory, boolean deleteExtractedPaths) {
        if (source != null) {
            assert(source.length > 0);
            for (Path path: source) assert(!path.isAbsolute());
        }
        assert(targetDirectory != null);
        assert(targetDirectory.isAbsolute());

        new ZipToNativeExtractor(model, source, targetDirectory, deleteExtractedPaths).execute();
    }

    private ZipToNativeExtractor(ZipModel model, Path[] source, Path targetDirectory, boolean deleteExtractedPaths) {
        assert(targetDirectory.isAbsolute());
        this.model = model;
        this.targetDirectory = targetDirectory;
        if (source != null) {
            sourceDirectory = source[0].getParent();
            filter = new IncludePathFilter(source);
        }
        this.deleteExtractedPaths = deleteExtractedPaths;

        long totalValue = ZipUtils.getSize(model, deleteExtractedPaths? null : filter);
        progress = new ExtendedPathProgress(RootFrame.getRoot(),
                new ResourceKey("dlg.zip.extract.progress.title"),
                new ResourceKey("dlg.zip.extract.progress.descr", model.getZip(), targetDirectory),
                totalValue) {
            @Override
            protected void onCancel() {
                ZipToNativeExtractor.this.cancel(false);
                ZipUtils.unlockAndDeleteTempZip(ZipToNativeExtractor.this.model, tmpZip);
            }
        };
        //initTextField overwriteProcessor after progress
        overwriteProcessor = new ZipExtractorOverwriteProcessor();
    }

    @Override
    protected void process(List<PublishedPath> paths) {
        progress.beforeProcessing(paths);
    }

    @Override
    protected void done() {
        progress.close();
    }

    @Override
    protected Void doInBackground() {
        FileOperationWorkers.add(this);
        try {
            if (deleteExtractedPaths) {
                tryToMove();
            } else {
                tryToCopy();
            }
        } catch (Exception e) {
            Errors.showError(e);
        } finally {
            ZipUtils.unlockAndDeleteTempZip(model, tmpZip);
        }
        return null;
    }

    private void tryToCopy() throws Exception {
System.out.println("TRY TO COPY");
        try (
            ZipInputStream zis = new ZipInputStream(Files.newInputStream(model.getZip()));
            ZipFile zipFile = new ZipFile(model.getZip().toFile())
        ){
            this.zis = zis;
            this.zipFile = zipFile;
            ZipEntry entry;
            while((entry = zis.getNextEntry()) != null) {
                if (ZipToNativeExtractor.this.isCancelled()) break;
                extractEntry(entry);
            }
        }
    }

    private void tryToMove() throws Exception {
        if ((tmpZip = ZipUtils.createTempZip(model)) == null) return;
        try (
            ZipInputStream zis = new ZipInputStream(Files.newInputStream(model.getZip()));
            ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(tmpZip));
            ZipFile zipFile = new ZipFile(model.getZip().toFile())
        ){
            this.zis = zis;
            this.zipFile = zipFile;
            ZipEntry entry;
            while((entry = zis.getNextEntry()) != null) {
                if (ZipToNativeExtractor.this.isCancelled()) break;
                if (!extractEntry(entry)) {
                    //publish not extracted entry
                    publishEntry(entry);
                    zos.putNextEntry(entry);
                    int written = ZipUtils.writeEntry(zis, zos);
                    progress.afterProcessing(written);
                }
            }
        }
        if (!ZipToNativeExtractor.this.isCancelled()) {
            ZipUtils.replaceZipWithTempZip(model, tmpZip);
        }
    }

    private void publishEntry(ZipEntry entry) {
        ZipToNativeExtractor.this.publish(new ZipPublishedPath(zipFile.getEntry(entry.getName())));
    }

    private boolean extractEntry(ZipEntry entry) throws Exception {
        Path entryPath = Paths.get(entry.getName());
        if (filter != null && !filter.include(entryPath)) return false;
        publishEntry(entry);
        overwriteProcessor.checkAndProcess(entryPath);
        return true;
    }

    private class ZipExtractorOverwriteProcessor extends OverwriteProcessor {
        private ZipExtractorOverwriteProcessor() {
            super(model.getProcessor(), NativePathProcessor.getInstance(), progress);
        }

        @Override
        protected Path getTarget(Path source) {
            Path target = sourceDirectory != null? sourceDirectory.relativize(source) : source;
            target = targetDirectory.resolve(target);
            return target;
        }

        @Override
        protected void overwriteFile(Path source, Path target) throws Exception {
            processFile(source, target);
        }

        @Override
        protected void processFile(Path source, Path target) throws Exception {
System.out.println(source + " " + target);
            Path parent = target.getParent();
            if (parent != null && !PathUtils.existsNoFollowLink(parent)) {
                Files.createDirectories(parent);
            }
            try (
                BufferedOutputStream out = new BufferedOutputStream(Files.newOutputStream(target))
            ) {
                int written = ZipUtils.writeEntry(zis, out);
                progress.afterProcessing(written);
            }
        }

        @Override
        protected void processDirectory(Path source, Path target) throws Exception {
            Files.createDirectories(target);
        }

        @Override
        protected void cancel() {
            ZipToNativeExtractor.this.cancel(false);
        }
    }
}

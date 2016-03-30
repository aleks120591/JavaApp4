package lenacom.filer.zip;

import lenacom.filer.config.ResourceKey;
import lenacom.filer.root.FileOperationWorkers;
import lenacom.filer.message.Errors;
import lenacom.filer.path.processor.OverwriteProcessor;
import lenacom.filer.progress.ExtendedPathProgress;
import lenacom.filer.progress.PublishedPath;
import lenacom.filer.root.RootFrame;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

class ZipToZipExtractor extends SwingWorker<Void, PublishedPath> {
    private Path tmpSourceZip;
    private Path tmpTargetZip;
    private ZipModel sourceModel;
    private ZipModel targetModel;
    private Path sourceDirectory = null; //can be null = zip root
    private Path targetDirectory = null; //can be null = zip root
    private boolean deleteExtractedPaths = false;
    private IncludePathFilter sourceFilter;
    private ExcludePathFilter targetFilter;
    private ExtendedPathProgress progress;
    private ZipExtractorOverwriteProcessor overwriteProcessor;
    private ZipInputStream sourceIn;
    private ZipOutputStream targetOut;
    private ZipFile sourceZipFile;
    private List<Path> filesAlreadyAddedInTargetZip;

    static void copy(ZipModel sourceModel, Path[] source, ZipModel targetModel, Path targetDirectory) {
        process(sourceModel, source, targetModel, targetDirectory, false);
    }

    static void move(ZipModel sourceModel, Path[] source, ZipModel targetModel, Path targetDirectory) {
        process(sourceModel, source, targetModel, targetDirectory, true);
    }

    private static void process(ZipModel sourceModel, Path[] source, ZipModel targetModel, Path targetDirectory, boolean deleteExtractedPaths) {
        assert(source.length > 0);
        for (Path path: source) assert(!path.isAbsolute());
        assert(targetDirectory == null || !targetDirectory.isAbsolute());
        new ZipToZipExtractor(sourceModel, source, targetModel, targetDirectory, deleteExtractedPaths).execute();
    }

    private ZipToZipExtractor(ZipModel sourceModel, Path[] source, ZipModel targetModel, Path targetDirectory, boolean deleteExtractedPaths) {
        this.sourceModel = sourceModel;
        this.targetModel = targetModel;
        this.targetDirectory = targetDirectory;
        sourceDirectory = source[0].getParent();
        this.deleteExtractedPaths = deleteExtractedPaths;
        sourceFilter = new IncludePathFilter(source);

        long totalValue = ZipUtils.getSize(targetModel, null) +
            ZipUtils.getSize(sourceModel, deleteExtractedPaths? null : sourceFilter);
        progress = new ExtendedPathProgress(RootFrame.getRoot(),
                new ResourceKey("dlg.zip.extract.progress.title"),
                new ResourceKey("dlg.zip.extract.progress.descr", sourceModel.getZip(), targetModel.getZip()),
                totalValue) {
            @Override
            protected void onCancel() {
                ZipToZipExtractor.this.cancel(false);
                ZipUtils.unlockAndDeleteTempZip(ZipToZipExtractor.this.sourceModel, tmpSourceZip);
                ZipUtils.unlockAndDeleteTempZip(ZipToZipExtractor.this.targetModel, tmpTargetZip);
            }
        };
        //initTextField overwriteProcessor after progress
        overwriteProcessor = new ZipExtractorOverwriteProcessor();

        filesAlreadyAddedInTargetZip = new ArrayList<>();
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
            ZipUtils.unlockAndDeleteTempZip(sourceModel, tmpSourceZip);
            ZipUtils.unlockAndDeleteTempZip(targetModel, tmpTargetZip);
        }
        return null;
    }

    private void tryToCopy() throws Exception {
        if ((tmpTargetZip = ZipUtils.createTempZip(targetModel)) == null) return;
        try (
            ZipInputStream sourceIn = new ZipInputStream(Files.newInputStream(sourceModel.getZip()));
            ZipFile zipFile = new ZipFile(sourceModel.getZip().toFile());
            ZipOutputStream targetOut = new ZipOutputStream(Files.newOutputStream(tmpTargetZip))
        ){
            this.sourceIn = sourceIn;
            this.sourceZipFile = zipFile;
            this.targetOut = targetOut;
            ZipEntry entry;
            while((entry = sourceIn.getNextEntry()) != null) {
                if (ZipToZipExtractor.this.isCancelled()) break;
                extractEntry(entry);
            }
            writeAllTargetEntries();
        }
        if (!ZipToZipExtractor.this.isCancelled()) {
            ZipUtils.replaceZipWithTempZip(targetModel, tmpTargetZip);
        }
    }

    private void tryToMove() throws Exception {
        if ((tmpSourceZip = ZipUtils.createTempZip(sourceModel)) == null) return;
        if ((tmpTargetZip = ZipUtils.createTempZip(targetModel)) == null) return;
        try (
            ZipInputStream sourceIn = new ZipInputStream(Files.newInputStream(sourceModel.getZip()));
            ZipFile sourceZipFile = new ZipFile(sourceModel.getZip().toFile());
            ZipOutputStream sourceOut = new ZipOutputStream(Files.newOutputStream(tmpSourceZip));
            ZipOutputStream targetOut = new ZipOutputStream(Files.newOutputStream(tmpTargetZip))
        ){
            this.sourceIn = sourceIn;
            this.sourceZipFile = sourceZipFile;
            this.targetOut = targetOut;
            ZipEntry entry;
            while((entry = sourceIn.getNextEntry()) != null) {
                if (ZipToZipExtractor.this.isCancelled()) break;
                if (!extractEntry(entry)) {
                    //publish not extracted source entry
                    publishEntry(sourceZipFile, entry);
                    sourceOut.putNextEntry(entry);
                    int written = ZipUtils.writeEntry(sourceIn, sourceOut);
                    progress.afterProcessing(written);
                }
            }
            writeAllTargetEntries();
        }
        if (!ZipToZipExtractor.this.isCancelled()) {
            ZipUtils.replaceZipWithTempZip(sourceModel, tmpSourceZip);
            ZipUtils.replaceZipWithTempZip(targetModel, tmpTargetZip);
        }
    }

    private void publishEntry(ZipFile zipFile, ZipEntry entry) {
        ZipToZipExtractor.this.publish(new ZipPublishedPath(zipFile.getEntry(entry.getName())));
    }

    private void writeAllTargetEntries() throws IOException {
        try (
            ZipInputStream targetIn = new ZipInputStream(Files.newInputStream(targetModel.getZip()));
            ZipFile targetZipFile = new ZipFile(targetModel.getZip().toFile())
        ) {
            ZipEntry entry;
            while((entry = targetIn.getNextEntry()) != null) {
                if (ZipToZipExtractor.this.isCancelled()) break;
                Path entryPath = Paths.get(entry.getName());
                //we publish all target entries
                //because we included their size while creating a progress dialog
                publishEntry(targetZipFile, entry);
                if (targetFilter != null && !targetFilter.include(entryPath)) continue;
                targetOut.putNextEntry(entry);
                int written = ZipUtils.writeEntry(targetIn, targetOut);
                progress.afterProcessing(written);
            }
        }
    }

    private boolean extractEntry(ZipEntry entry) throws Exception {
        Path entryPath = Paths.get(entry.getName());
        if (!sourceFilter.include(entryPath)) return false;
        //publish extracted source entry
        publishEntry(sourceZipFile, entry);
        overwriteProcessor.checkAndProcess(entryPath);
        return true;
    }

    private class ZipExtractorOverwriteProcessor extends OverwriteProcessor {
        private ZipExtractorOverwriteProcessor() {
            super(sourceModel.getProcessor(), new TargetPathProcessor(), progress);
        }

        @Override
        protected Path getTarget(Path source) {
            Path target = sourceDirectory != null? sourceDirectory.relativize(source) : source;
            if (targetDirectory != null) target = targetDirectory.resolve(target);
            return target;
        }

        @Override
        protected void overwriteFile(Path source, Path target) throws Exception {
            if (targetFilter == null) {
                targetFilter = new ExcludePathFilter(new Path[]{ target });
            }
            else {
                targetFilter.addPath(target);
            }
            processFile(source, target);
        }

        @Override
        protected void processFile(Path source, Path target) throws Exception {
            ZipEntry entry = new ZipEntry(target.toString());
            targetOut.putNextEntry(entry);
            int written = ZipUtils.writeEntry(sourceIn, targetOut);
            progress.afterProcessing(written);
            filesAlreadyAddedInTargetZip.add(target);
        }

        @Override
        protected void processDirectory(Path source, Path target) throws Exception {
            //do nothing
        }

        @Override
        protected void cancel() {
            ZipToZipExtractor.this.cancel(false);
        }
    }

    private class TargetPathProcessor extends ZipPathProcessor {
        private TargetPathProcessor() {
            super(targetModel);
        }

        @Override
        public boolean fileExists(Path path) {
            //we'll check also being added/removed files
            ZipEntry entry = targetModel.getEntry(path);
            return entry != null && !entry.isDirectory() && (targetFilter == null || targetFilter.include(path)) ||
                    filesAlreadyAddedInTargetZip.contains(path);
        }
    }
}

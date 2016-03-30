package lenacom.filer.zip;

import lenacom.filer.config.ResourceKey;
import lenacom.filer.root.FileOperationWorkers;
import lenacom.filer.message.Errors;
import lenacom.filer.path.PathAttributes;
import lenacom.filer.path.PathUtils;
import lenacom.filer.path.processor.NativePathProcessor;
import lenacom.filer.path.processor.OverwriteProcessor;
import lenacom.filer.path.processor.ReadonlyProcessor;
import lenacom.filer.path.processor.SafePathVisitor;
import lenacom.filer.progress.ExtendedPathProgress;
import lenacom.filer.progress.NativePublishedPath;
import lenacom.filer.progress.PublishedPath;
import lenacom.filer.root.RootFrame;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

final class ZipBuilder extends SwingWorker<Void, PublishedPath> {
    private ZipModel model;
    private ExcludePathFilter filter;
    private Path[] pathsToAdd;
    private Map<Path, Path> sourceToTarget;
    private ZipOutputStream zos;
    private boolean overwrite;
    private ExtendedPathProgress progress;
    private boolean deleteAddedPaths = false;
    private List<Path> alreadyAddedFiles;
    private Path targetDirectory = null; //can be null = zip root
    private Path tmpZip;

    static void build(ZipBuilderParameters params) {
        rebuild(params, true);
    }

    static void rebuild(ZipBuilderParameters params) {
        rebuild(params, false);
    }

    private static void rebuild(ZipBuilderParameters params, boolean overwrite) {
        new ZipBuilder(params, overwrite).execute();
    }

    private ZipBuilder(ZipBuilderParameters builderParams, boolean overwrite) {
        this.model = builderParams.getModel();
        filter = builderParams.getFilter();
        pathsToAdd = builderParams.getPathsToAdd();
        sourceToTarget = builderParams.getSourceToTarget();
        deleteAddedPaths = builderParams.isDeleteAddedPaths();
        targetDirectory = builderParams.getTargetDirectory();
        this.overwrite = overwrite;

        int totalValue = 0;
        if (!overwrite) totalValue += ZipUtils.getSize(model, filter);
        if (pathsToAdd != null) totalValue += PathUtils.getSizeWithChildren(pathsToAdd);
        progress = new ExtendedPathProgress(RootFrame.getRoot(),
                new ResourceKey("dlg.zip.pack.progress.title"),
                new ResourceKey("dlg.zip.pack.progress.descr", model.getZip()),
                totalValue) {
            @Override
            protected void onCancel() {
                ZipBuilder.this.cancel(false);
                ZipUtils.unlockAndDeleteTempZip(model, tmpZip);
            }
        };
    }

    @Override
    protected Void doInBackground() {
        FileOperationWorkers.add(this);
        try {
            tryToBuild();
        } catch (Exception e) {
            Errors.showError(e);
        } finally {
            ZipUtils.unlockAndDeleteTempZip(model, tmpZip);
        }
        return null;
    }

    private void tryToBuild() throws Exception {
        if ((tmpZip = ZipUtils.createTempZip(model)) == null) return;
        if (overwrite) {
            try(ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(tmpZip))) {
                this.zos = zos;
                if (pathsToAdd != null) addPaths();
            }
        } else {
            try(
                ZipInputStream zis = new ZipInputStream(Files.newInputStream(model.getZip()));
                ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(tmpZip))
            ) {
                this.zos = zos;

                //we add path first because some entries may be overwritten
                if (pathsToAdd != null) addPaths();

                ZipEntry entry;
                while((entry = zis.getNextEntry()) != null) {
                    if (ZipBuilder.this.isCancelled()) break;
                    Path entryPath = Paths.get(entry.getName());
                    if (filter != null && !filter.include(entryPath)) continue;
                    //entry with positive size
                    ZipPublishedPath publishedPath = new ZipPublishedPath(model.getEntry(entryPath));
                    ZipBuilder.this.publish(publishedPath);
                    Path renamedPath = rename(entryPath);
                    if (renamedPath != null) {
                        entry = new ZipEntry(renamedPath.toString());
                    }
                    zos.putNextEntry(entry);
                    int written = ZipUtils.writeEntry(zis, zos);
                    progress.afterProcessing(written);
                }
            }
        }
        if (!ZipBuilder.this.isCancelled()) {
            ZipUtils.replaceZipWithTempZip(model, tmpZip);
        }
    }

    private Path rename(Path path) {
        if (sourceToTarget != null) {
            for (Path key : sourceToTarget.keySet()) {
                if (path.startsWith(key)) {
                    //rename a file or all files of a renamed directory
                    return sourceToTarget.get(key).resolve(key.relativize(path));
                }
            }
        }
        return null;
    }

    @Override
    protected void process(List<PublishedPath> paths) {
        progress.beforeProcessing(paths);
    }

    @Override
    protected void done() {
        progress.close();
    }

    private void addPaths() throws IOException {
        if (!ZipBuilder.this.isCancelled()) {
            alreadyAddedFiles = new ArrayList<>();
            new ZipPathVisitor().execute();
        }
    }

    private class ZipPathVisitor extends SafePathVisitor {
        private ReadonlyProcessor readonlyProcessor;
        private ZipBuilderOverwriteProcessor overwriteProcessor;

        ZipPathVisitor() {
            super(pathsToAdd);
            if (deleteAddedPaths) {
                readonlyProcessor = new ZipPathReadonlyProcessor(this);
            }
            overwriteProcessor = new ZipBuilderOverwriteProcessor(this);
        }

        @Override
         public void cancel() {
            super.cancel();
            ZipBuilder.this.cancel(false);
        }

        @Override
        public boolean isCancelled() {
            return super.isCancelled() || ZipBuilder.this.isCancelled();
        }

        @Override
        protected void safePostVisitDirectory(Path dir) throws Exception {
            if (deleteAddedPaths) {
                if (PathAttributes.isReadonly(dir)) {
                    PathAttributes.setReadonly(dir, false);
                }
                try {
                    Files.delete(dir);
                } catch(DirectoryNotEmptyException e) {
                    //it's ok, if some files were skipped, a directory can be not empty
                }
            }
        }

        private void publish(Path path) {
            ZipBuilder.this.publish(new NativePublishedPath(getSourceDirectory(), path));
        }

        @Override
        protected void safePreVisitDirectory(Path dir, BasicFileAttributes attrs) throws Exception {
            publish(dir);
            if (sourceToTarget != null) {
                Path target = sourceToTarget.get(dir);
                if (target != null) {
                    overwriteProcessor.processDirectory(dir, target);
                    return;
                }
            }
            overwriteProcessor.checkAndProcess(dir);
        }

        @Override
        protected void safeVisitFile(Path file, BasicFileAttributes attr) throws Exception {
            publish(file);
            if (deleteAddedPaths) {
                readonlyProcessor.checkAndProcess(file);
            } else {
                ZipPathVisitor.this.processFile(file);
            }
        }

        private void processFile(Path source) throws Exception {
            if (Files.isSymbolicLink(source)) {
                return; //symbolic links are skipped
            }
            if (sourceToTarget != null) {
                Path target = sourceToTarget.get(source);
                if (target != null) {
                    if (PathUtils.isDirectory(source)) {
                        overwriteProcessor.processDirectory(source, target);
                    } else {
                        overwriteProcessor.overwriteFile(source, target);
                    }
                    return;
                }
            }
            overwriteProcessor.checkAndProcess(source);
        }
    }

    private class ZipPathReadonlyProcessor extends ReadonlyProcessor {

        private ZipPathReadonlyProcessor(SafePathVisitor visitor) {
            super(visitor, progress, "process.move");
        }

        @Override
        protected void process(Path path) throws Exception {
            ((ZipPathVisitor) visitor).processFile(path);
        }

        @Override
        protected void processReadonly(Path path) throws Exception {
            //deleteAddedPaths is true
            PathAttributes.setReadonly(path, false);
            ((ZipPathVisitor) visitor).processFile(path);
        }
    }

    private class ZipBuilderOverwriteProcessor extends OverwriteProcessor {
        private ZipPathVisitor visitor;

        private ZipBuilderOverwriteProcessor(ZipPathVisitor visitor) {
            super(NativePathProcessor.getInstance(), new ZipBuilderPathProcessor(), progress);
            this.visitor = visitor;
        }

        @Override
        protected Path getTarget(Path source) {
            Path target = visitor.getSourceDirectory().relativize(source);
            if (targetDirectory != null) target = targetDirectory.resolve(target);
            return target;
        }

        @Override
        protected void overwriteFile(Path source, Path target) throws Exception {
            if (filter == null) {
                filter = new ExcludePathFilter(new Path[]{ target });
            }
            else {
                filter.addPath(target);
            }
            processFile(source, target);
        }

        @Override
        protected void processFile(Path source, Path target) throws Exception {
            ZipEntry entry = new ZipEntry(target.toString());
            zos.putNextEntry(entry);
            try (InputStream in = Files.newInputStream(source)) {
                int written = ZipUtils.writeEntry(in, zos);
                progress.afterProcessing(written);
            }
            if (deleteAddedPaths) {
                Files.delete(source);
            }
            alreadyAddedFiles.add(target);
        }

        @Override
        protected void processDirectory(Path source, Path target) throws Exception {
            //we need to copy only an empty directory
            //all directories are deleted after visiting all their files
            boolean empty = true;
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(source)) {
                empty = !stream.iterator().hasNext();
            }
            if (empty) {
                ZipEntry entry = new ZipEntry(target.toString() + "/");
                zos.putNextEntry(entry);
            }
            progress.afterProcessing(Files.size(source));
        }

        @Override
        protected void cancel() {
            ZipBuilder.this.cancel(false);
        }
    }

    private class ZipBuilderPathProcessor extends ZipPathProcessor {
        private ZipBuilderPathProcessor() {
            super(model);
        }

        @Override
        public boolean fileExists(Path path) {
            //we'll check also being added/removed files
            ZipEntry entry = model.getEntry(path);
            return entry != null && !entry.isDirectory() && (filter == null || filter.include(path)) ||
                alreadyAddedFiles.contains(path);
        }
    }
}

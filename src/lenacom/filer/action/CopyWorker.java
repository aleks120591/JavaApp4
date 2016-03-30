package lenacom.filer.action;

import lenacom.filer.config.ResourceKey;
import lenacom.filer.message.Messages;
import lenacom.filer.root.FileOperationWorkers;
import lenacom.filer.message.Errors;
import lenacom.filer.path.PathUtils;
import lenacom.filer.path.processor.SafePathVisitor;
import lenacom.filer.progress.ExtendedPathProgress;
import lenacom.filer.progress.NativePublishedPath;
import lenacom.filer.progress.PathProgress;
import lenacom.filer.progress.PublishedPath;
import lenacom.filer.root.RootFrame;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public final class CopyWorker extends SwingWorker<Void, PublishedPath> {
    private CopyPathVisitor visitor;
    private ExtendedPathProgress progress;
    private CopyMoveOverwriteProcessor overwriteProcessor;
    private final CopyOption[] options = new CopyOption[]{StandardCopyOption.COPY_ATTRIBUTES, LinkOption.NOFOLLOW_LINKS};
    private AtomicInteger countCopiedSymlinks = new AtomicInteger(0);

    public CopyWorker(Path onePathSource, final Path target /*renamed*/) {
        Path[] source = new Path[]{ onePathSource };
        long size = PathUtils.getSizeWithChildren(source);
        long freeSize = target.getParent().toFile().getFreeSpace();
        if (size > freeSize) {
            Messages.showMessage("msg.not.enough.free.space");
            return;
        }
        visitor = new CopyPathVisitor(onePathSource);
        progress = new CopyProgress(source, target.getParent(), size);
        overwriteProcessor = new CopyOnePathOverwriteProcessor(visitor, progress, onePathSource, target);
    }

    public CopyWorker(Path[] source, final Path target) {
        long size = PathUtils.getSizeWithChildren(source);
        long freeSize = target.toFile().getFreeSpace();
        if (size > freeSize) {
            Messages.showMessage("msg.not.enough.free.space");
            return;
        }

        visitor = new CopyPathVisitor(source);
        progress = new CopyProgress(source, target, size);

        overwriteProcessor = new CopyOverwriteProcessor(visitor, progress) {
            @Override
            protected Path getTargetDirectory() {
                return target;
            }
        };
    }

    private class CopyProgress extends ExtendedPathProgress {
        private CopyProgress(Path[] source, Path target, long size) {
            super(RootFrame.getRoot(),
                    new ResourceKey("dlg.copy.progress.title"),
                    new ResourceKey("dlg.copy.progress.descr", source[0].getParent(), target),
                    size);
        }
        @Override
        protected void onCancel() {
            visitor.cancel();
        }
    }

    private void countCopiedSymlink(Path source) {
        if (Files.isSymbolicLink(source)) {
            countCopiedSymlinks.incrementAndGet();
        }
    }

    private class CopyOnePathOverwriteProcessor extends CopyOverwriteProcessor {
        private Path source;
        private Path target;

        protected CopyOnePathOverwriteProcessor(SafePathVisitor visitor, PathProgress progress, Path source, Path target) {
            super(visitor, progress);
            this.source = source;
            this.target = target;
            //we check if a new path already exists in RenameDialog
            setOverwritePolicy(OverwritePolicy.OVERWRITE_ALL);
        }

        @Override
        protected Path getTarget(Path source) {
            if (source.equals(this.getTargetDirectory())) return target;
            return target.resolve(this.source.relativize(source));
        }

        @Override
        protected Path getTargetDirectory() {
            //don't need it
            return target.getParent();
        }
    }

    private abstract class CopyOverwriteProcessor extends CopyMoveOverwriteProcessor {
        private CopyOverwriteProcessor(SafePathVisitor visitor, PathProgress progress) {
            super(visitor, progress, options);
        }

        @Override
        protected void processFile(Path source, Path target, CopyOption[] options) throws IOException {
            Files.copy(source, target, options);
            progress.afterProcessing(Files.size(source));
            countCopiedSymlink(source);
        }

        @Override
        protected void processDirectory(Path source, Path target, CopyOption[] options) throws IOException {
            Files.copy(source, target, options);
            progress.afterProcessing(Files.size(source));
        }
    }

    @Override
    protected Void doInBackground() {
        FileOperationWorkers.add(this);
        try {
            visitor.execute();
        } catch (Exception e) {
            Errors.showError(e);
        } finally {
            done();
        }
        return null;
    }

    @Override
    protected void done() {
        progress.close();
        int count = countCopiedSymlinks.get();
        if (count > 0) {
            Messages.showMessage("msg.copied.symlinks", count);
        }
    }

    @Override
    protected void process(List<PublishedPath> paths) {
        //this method is called before actual copying/moving the last path
        progress.beforeProcessing(paths);
    }

    private class CopyPathVisitor extends SafePathVisitor {
        private CopyPathVisitor(Path... source) {
            super(source);
        }

        protected void publish(Path path) {
            CopyWorker.this.publish(new NativePublishedPath(getSourceDirectory(), path));
        }

        @Override
        protected void safeVisitFile(Path file, BasicFileAttributes attrs) throws Exception {
            publish(file);
            overwriteProcessor.checkAndProcess(file);
        }

        @Override
        protected void safePreVisitDirectory(Path dir, BasicFileAttributes attrs) throws Exception {
            publish(dir);
            overwriteProcessor.checkAndProcess(dir);
        }

        @Override
        protected void safePostVisitDirectory(Path dir) {
            //do nothing
        }
    }
}

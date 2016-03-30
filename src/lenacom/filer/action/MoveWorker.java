package lenacom.filer.action;

import lenacom.filer.config.ResourceKey;
import lenacom.filer.message.Messages;
import lenacom.filer.root.FileOperationWorkers;
import lenacom.filer.message.Errors;
import lenacom.filer.path.PathAttributes;
import lenacom.filer.path.PathUtils;
import lenacom.filer.path.processor.ReadonlyProcessor;
import lenacom.filer.path.processor.SafePathVisitor;
import lenacom.filer.progress.ExtendedPathProgress;
import lenacom.filer.progress.NativePublishedPath;
import lenacom.filer.progress.PublishedPath;
import lenacom.filer.root.RootFrame;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

public final class MoveWorker extends SwingWorker<Void, PublishedPath> {
    private final MovePathVisitor visitor;
    private ExtendedPathProgress progress;
    private ReadonlyProcessor readonlyProcessor;
    private CopyMoveOverwriteProcessor overwriteProcessor;
    private final CopyOption[] options = new CopyOption[]{LinkOption.NOFOLLOW_LINKS};

    public MoveWorker(Path[] source, final Path target) {
        visitor = new MovePathVisitor(source);

        long size = PathUtils.getSizeWithChildren(source);
        long freeSize = target.toFile().getFreeSpace();
        if (size > freeSize) {
            Messages.showMessage("msg.not.enough.free.space");
            return;
        }

        progress = new ExtendedPathProgress(RootFrame.getRoot(),
                new ResourceKey("dlg.move.progress.title"),
                new ResourceKey("dlg.move.progress.descr", source[0].getParent(), target),
                size) {
            @Override
            protected void onCancel() {
                visitor.cancel();
            }
        };

        overwriteProcessor = new CopyMoveOverwriteProcessor(visitor, progress, options) {
            @Override
            protected Path getTargetDirectory() {
                return target;
            }

            @Override
            protected void processFile(Path source, Path target, CopyOption[] options) throws IOException {
                Files.move(source, target, options);
                progress.afterProcessing(Files.size(target));
            }

            @Override
            protected void processDirectory(Path source, Path target, CopyOption[] options) throws IOException {
                //we can't move a not empty directory, so we copy it before visiting and delete it after visiting
                Files.copy(source, target, options);
                progress.afterProcessing(Files.size(target));
            }
        };

        readonlyProcessor = new ReadonlyProcessor(visitor, progress, "process.move") {
            @Override
            protected void process(Path path) throws Exception {
                overwriteProcessor.checkAndProcess(path);
            }

            @Override
            protected void processReadonly(Path path) throws Exception {
                PathAttributes.setReadonly(path, false);
                process(path);
            }
        };

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
    }

    @Override
    protected void process(List<PublishedPath> paths) {
        //this method is called before actual copying/moving the last path
        progress.beforeProcessing(paths);
    }

    private final class MovePathVisitor extends SafePathVisitor {
        private MovePathVisitor(Path[] source) {
            super(source);
        }

        private void publish(Path path) {
            MoveWorker.this.publish(new NativePublishedPath(getSourceDirectory(), path));
        }

        @Override
        protected void safeVisitFile(Path file, BasicFileAttributes attr) throws Exception {
            publish(file);
            readonlyProcessor.checkAndProcess(file);
        }

        @Override
        protected void safePreVisitDirectory(Path dir, BasicFileAttributes attrs) throws Exception {
            publish(dir);
            //we can't move a not empty directory, so we copy it before visiting and delete it after visiting
            overwriteProcessor.checkAndProcess(dir);
        }

        @Override
        protected void safePostVisitDirectory(Path dir) throws Exception {
            //we can't move a not empty directory, so we copy it before visiting and delete it after visiting
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
}

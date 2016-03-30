package lenacom.filer.action;

import lenacom.filer.config.ResourceKey;
import lenacom.filer.root.FileOperationWorkers;
import lenacom.filer.message.Errors;
import lenacom.filer.path.PathAttributes;
import lenacom.filer.path.PathUtils;
import lenacom.filer.path.processor.ReadonlyProcessor;
import lenacom.filer.path.processor.SafePathVisitor;
import lenacom.filer.progress.BasicPathProgress;
import lenacom.filer.progress.NativePublishedPath;
import lenacom.filer.progress.PublishedPath;
import lenacom.filer.root.RootFrame;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

public final class DeleteWorker extends SwingWorker<Void, PublishedPath> {
    private final SafePathVisitor visitor;
    private final BasicPathProgress progress;
    private final ReadonlyProcessor readonlyProcessor;

    public DeleteWorker(Path[] paths) {
        visitor = new DeletePathVisitor(paths);

        progress = new BasicPathProgress(RootFrame.getRoot(),
                new ResourceKey("dlg.delete.progress.title"),
                new ResourceKey("dlg.delete.progress.descr",
                paths[0].getParent().toString()),
                PathUtils.getSizeWithChildren(paths), false) {
            @Override
            protected void onCancel() {
                visitor.cancel();
            }
        };
        readonlyProcessor = new ReadonlyProcessor(visitor, progress, "process.delete") {
            @Override
            protected void process(Path path) throws IOException{
                try {
                    long size = Files.size(path);
                    Files.delete(path);
                    progress.afterProcessing(size);
                } catch(DirectoryNotEmptyException e) {
                    //it's ok, if some files were skipped, a directory can be not empty
                }
            }

            @Override
            protected void processReadonly(Path path) throws IOException {
                PathAttributes.setReadonly(path, false);
                process(path);
            }
        };
    }

    @Override
    protected void done() {
        progress.close();
    }

    @Override
    protected Void doInBackground() {
        FileOperationWorkers.add(this);
        try {
            visitor.execute();
        } catch (Exception e) {
            Errors.showError(e);
        }
        return null;
    }

    @Override
    protected void process(List<PublishedPath> paths) {
        progress.beforeProcessing(paths);
    }

    private final class DeletePathVisitor extends SafePathVisitor {
        private DeletePathVisitor(Path[] paths) {
            super(paths);
        }

        private void publish(Path path) {
            DeleteWorker.this.publish(new NativePublishedPath(getSourceDirectory(), path));
        }

        @Override
        protected void safeVisitFile(Path file, BasicFileAttributes attr) throws Exception {
            publish(file);
            readonlyProcessor.checkAndProcess(file);
        }

        @Override
        protected void safePostVisitDirectory(Path dir) throws Exception {
            publish(dir);
            readonlyProcessor.checkAndProcess(dir);
        }

        @Override
        protected void safePreVisitDirectory(Path dir, BasicFileAttributes attrs) {
            //do nothing
        }
    }
}
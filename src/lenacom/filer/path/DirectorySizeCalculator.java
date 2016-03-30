package lenacom.filer.path;

import lenacom.filer.message.Errors;
import lenacom.filer.message.Messages;
import lenacom.filer.panel.DirectoryRow;
import lenacom.filer.zip.ZipPathProcessor;
import lenacom.filer.zip.ZipWorker;

import javax.swing.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Set;

public class DirectorySizeCalculator {
    public static void calculate(DirectoryRow row) {
        new NativeDirectorySizeWorker(row).execute();
    }

    public static void calculate(ZipWorker zipWorker, DirectoryRow row) {
        new ZipDirectorySizeWorker(zipWorker, row).execute();
    }

    private static final class NativeDirectorySizeWorker extends SwingWorker<Long, Long> {
        private DirectoryRow row;
        private long size = 0;

        private NativeDirectorySizeWorker(DirectoryRow row) {
            this.row = row;
        }

        @Override
        protected Long doInBackground() {
            try {
                NativePathVisitor visitor = new NativePathVisitor();
                try {
                    Files.walkFileTree(row.getPath(), visitor);
                } catch (AccessDeniedException e) {
                    Messages.showMessage("err.access.denied", e.getMessage());
                    return 0L;
                }
            } catch (Exception e) {
                Errors.showError(e);
                return 0L;
            }
            return size;
        }

        @Override
        protected void process(List<Long> chunks) {
            for (Long chunk : chunks) size += chunk;
            row.setSize(size);
        }

        @Override
        protected void done() {
            row.setSize(size);
            //setting a directory size as an attribute will cause a path modified event, the row will be recreated
            PathAttributes.setDirectorySize(row.getPath(), size);
        }

        private final class NativePathVisitor extends SimpleFileVisitor<Path> {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attr) {
                publish(attr.size());
                return FileVisitResult.CONTINUE;
            }
        }
    }

    private static final class ZipDirectorySizeWorker extends SwingWorker<Long, Long> {
        private ZipWorker zipWorker;
        private DirectoryRow row;
        private long size = 0;

        private ZipDirectorySizeWorker(ZipWorker zipWorker, DirectoryRow row) {
            this.zipWorker = zipWorker;
            this.row = row;
        }

        private void processParent(Path parent) {
            Set<Path> children = zipWorker.getChildren(parent);
            if (children == null) return;
            for (Path child: children) {
                ZipPathProcessor processor = zipWorker.getPathProcessor();
                if (processor.isDirectory(child)) {
                    processParent(child);
                } else {
                    publish(processor.getSize(child));
                }
            }
        }

        @Override
        protected Long doInBackground() {
            try {
                processParent(row.getPath());
            } catch (Exception e) {
                Errors.showError(e);
            }
            return size;
        }

        @Override
        protected void process(List<Long> chunks) {
            for (Long chunk : chunks) size += chunk;
            row.setSize(size);
        }

        @Override
        protected void done() {
            row.setSize(size);
            zipWorker.setDirectorySize(row.getPath(), size);
        }
    }
}

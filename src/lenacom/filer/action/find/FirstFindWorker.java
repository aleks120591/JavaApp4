package lenacom.filer.action.find;

import lenacom.filer.message.Errors;
import lenacom.filer.zip.ZipPathProcessor;
import lenacom.filer.zip.ZipWorker;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Set;

class FirstFindWorker extends AbstractFindWorker {
    private ZipWorker zipWorker;
    private boolean findDirectory = false;

    FirstFindWorker(FindDialog dialog, Path base, FindParameters params) {
        super(dialog, base, params);
        findDirectory = params.getContains() == null;
    }

    FirstFindWorker(FindDialog dialog, Path base, FindParameters params, ZipWorker zipWorker) {
        this(dialog, base, params);
        this.zipWorker = zipWorker;
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
        if (zipWorker == null) {
            FindVisitor visitor = new FindVisitor();
            Files.walkFileTree(base, visitor);
        }
        else {
            processZipPath(base);
        }
    }

    private void publishZipFile(Path path) {
        //we don't support searching of text in files inside a zip archive
        //the params 'size' is not needed in this case
        publishFile(path, 0);
    }

    private void processZipPath(Path parent) {
        if (FirstFindWorker.this.isCancelled()) return;
        ZipPathProcessor processor = zipWorker.getPathProcessor();
        Set<Path> children = zipWorker.getChildren(parent);
        if (children == null) return;
        for (Path child: children) {
            if (processor.isDirectory(child)) {
                publishDirectory(child);
                processZipPath(child);
            } else {
                publishZipFile(child);
            }
        }
    }

    private final class FindVisitor extends SimpleFileVisitor<Path> {

        private FileVisitResult result() {
            return FirstFindWorker.this.isCancelled()? FileVisitResult.TERMINATE : FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attr) {
            publishFile(file, attr.size());
            return result();
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
            if (findDirectory) publishDirectory(dir);
            return result();
        }
    }

}

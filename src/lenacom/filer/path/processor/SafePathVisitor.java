package lenacom.filer.path.processor;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public abstract class SafePathVisitor extends SafeProcessor {
    private final Path[] source;
    private final SimpleFileVisitor<Path> visitor;
    //this field is accessed from different threads
    private volatile boolean cancelled = false;
    private final Path sourceDirectory;

    public SafePathVisitor(Path[] source) {
        this.source = source;
        this.visitor = new InnerPathVisitor();
        this.sourceDirectory = source[0].getParent();
    }

    public void execute() {
        for (Path path: source) {
            try {
                Files.walkFileTree(path, visitor);
            } catch (Exception e) {
                processError(e);
            }
        }
    }

    private FileVisitResult checkCancelled() {
        return isCancelled() ? FileVisitResult.TERMINATE : FileVisitResult.CONTINUE;
    }

    private class InnerPathVisitor extends SimpleFileVisitor<Path> {

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attr) {
            boolean retry = false;
            do {
                try {
                    if (!isCancelled()) {
                        safeVisitFile(file, attr);
                    }
                } catch (Exception e) {
                    retry = processError(e);
                }
            } while (retry);
            return checkCancelled();
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
            boolean retry = false;
            do {
                try {
                    if (!isCancelled()) {
                        safePostVisitDirectory(dir);
                    }
                } catch (Exception e) {
                    retry = processError(e);
                }
            } while (retry);
            return checkCancelled();
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
            boolean retry = false;
            do {
                try {
                    if (!isCancelled()) {
                        safePreVisitDirectory(dir, attrs);
                    }
                } catch (Exception e) {
                    retry = processError(e);
                }
            } while (retry);
            return checkCancelled();
        }
    }

    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void cancel() {
        cancelled = true;
    }

    public Path getSourceDirectory() {
        return sourceDirectory;
    }

    protected abstract void safePostVisitDirectory(Path dir) throws Exception;
    protected abstract void safePreVisitDirectory(Path dir, BasicFileAttributes attrs) throws Exception;
    protected abstract void safeVisitFile(Path file, BasicFileAttributes attr) throws Exception;
}
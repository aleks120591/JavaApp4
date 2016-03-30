package lenacom.filer.path;

import lenacom.filer.message.Errors;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class CountFiles extends SimpleFileVisitor<Path> {
    private Result result = new Result();

    public static Result countFiles(Path... paths) {
        CountFiles visitor = new CountFiles();
        for (Path path: paths) {
            try {
                Files.walkFileTree(path, visitor);
            } catch (IOException e) {
                Errors.showError(e);
            }
        }
        return visitor.result;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attr) {
        result.countFiles++;
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
        result.countDirs++;
        return FileVisitResult.CONTINUE;
    }
    
    public static final class Result {
        private int countFiles = 0;
        private int countDirs = 0;

        public int getCountFiles() {
            return countFiles;
        }

        public int getCountDirs() {
            return countDirs;
        }
    }
}
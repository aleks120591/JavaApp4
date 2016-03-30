package lenacom.filer.path;

import lenacom.filer.config.Configuration;

import java.awt.*;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

public class PathUtils {
    private static final String PREFIX = "filer";

    public static boolean isSymlinkSupported() {
        Boolean isSymlinkSupported = Configuration.getBoolean(Configuration.IS_SYMLINK_SUPPORTED);
        if (isSymlinkSupported == null) {
            synchronized (PathUtils.class) {
                Path tmpTarget = null, tmpSymlink = null;
                try {
                    tmpTarget = Files.createTempFile(PREFIX, ".target");
                    tmpSymlink = tmpTarget.resolveSibling(tmpTarget.getFileName() + ".symlink");
                    try {
                        Files.createSymbolicLink(tmpSymlink, tmpTarget);
                        isSymlinkSupported = true;
                    } catch (UnsupportedOperationException x) {
                        isSymlinkSupported = false;
                    }
                    Configuration.setBoolean(Configuration.IS_SYMLINK_SUPPORTED, isSymlinkSupported);
                } catch (IOException e) {
                    e.printStackTrace();
                    isSymlinkSupported = true;
                } finally {
                    deleteTempFile(tmpTarget);
                    deleteTempFile(tmpSymlink);
                }
            }
        }
        return isSymlinkSupported;
    }

    public static boolean isOpenSupported() {
        return Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN);
    }

    private static void deleteTempFile(Path path) {
        if (path != null) {
            try {
                Files.deleteIfExists(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String getName(Path path) {
        return path == null? "" :
            path.getFileName() == null? path.toString() :
            path.getFileName().toString();
    }

    public static Path getDefaultRoot() {
        for (Path root: FileSystems.getDefault().getRootDirectories()) {
            if (Files.isReadable(root)) return root;
        }
        throw new RuntimeException("No readable root directory!");
    }

    public static Date getLastModified(Path path) throws IOException {
        return fileTimeToDate(Files.getLastModifiedTime(path, LinkOption.NOFOLLOW_LINKS));
    }

    private static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("dd.MM.yy HH:mm");
    private static final SimpleDateFormat DATE_TIME_FORMAT_EXT = new SimpleDateFormat("dd.MM.yy HH:mm:ss");
    public static String formatDate(Date date) {
        if (date == null) return "";
        return DATE_TIME_FORMAT.format(date);
    }

    public static String formatDateExt(Date date) {
        if (date == null) return "";
        return DATE_TIME_FORMAT_EXT.format(date);
    }

    private static Date fileTimeToDate(FileTime time) {
        return new Date(time.toMillis());
    }

    public static String formatDateExt(FileTime time) {
        if (time == null) return "";
        return formatDateExt(fileTimeToDate(time));
    }

    public static List<Path> getRoots() {
        List<Path> roots = new ArrayList<>();
        for (Path root: FileSystems.getDefault().getRootDirectories()) {
            roots.add(root);
        }
        return roots;
    }

    public static Path getClosestExistentParent(Path path) {
        while (path != null && (Files.notExists(path, LinkOption.NOFOLLOW_LINKS) || !Files.isReadable(path))) {
            path = path.getParent();
        }
        return path != null? path : getDefaultRoot();
    }

    private static Pattern zipPattern = Pattern.compile(".*\\.zip$|.*\\.jar$|.*\\.war$");
    public static boolean isZip(Path path) {
        String name = getName(path);
        return zipPattern.matcher(name).matches();
    }

    public static String getZipExtension() {
        return "zip";
    }

    private static class PathSize extends SimpleFileVisitor<Path> {
        private long size = 0;

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            size += attrs.size();
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            size += Files.size(dir); //Files.size() differs from attrs
            return FileVisitResult.CONTINUE;
        }
    }

    public static long getSizeWithChildren(Path[] source) {
        PathSize visitor = new PathSize();
        for (Path path: source) {
            try {
                Files.walkFileTree(path, visitor);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return visitor.size;
    }

    public static Path createTempFileInDirectory(Path directory) throws IOException {
        Path path = Files.createTempFile(directory, PREFIX, "");
        path.toFile().deleteOnExit();
        return path;
    }

    public static Path createTempFile() throws IOException {
        Path path = Files.createTempFile(PREFIX, "");
        path.toFile().deleteOnExit();
        return path;
    }

    public static Path createTempFile(String suffix) throws IOException {
        Path path = Files.createTempFile(PREFIX, suffix);
        path.toFile().deleteOnExit();
        return path;
    }

    public static Path createTempDirectoryInDirectory(Path directory) throws IOException {
        Path path = Files.createTempDirectory(directory, PREFIX);
        path.toFile().deleteOnExit();
        return path;
    }

    public static Path createTempDirectory() throws IOException {
        Path path = Files.createTempDirectory(PREFIX);
        path.toFile().deleteOnExit();
        return path;
    }

    public static boolean existsNoFollowLink(Path path) {
        return Files.exists(path, LinkOption.NOFOLLOW_LINKS);
    }

    public static boolean existsFollowLink(Path path) {
        return Files.exists(path);
    }

    public static boolean isDirectory(Path path) {
        return Files.isDirectory(path); //follow links
    }
}

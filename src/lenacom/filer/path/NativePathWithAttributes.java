package lenacom.filer.path;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.Date;
import java.util.Map;

public class NativePathWithAttributes {
    private Path path;
    private boolean isDirectory;
    private boolean isSymlink;
    private Long size;
    private Date lastModified;

    public NativePathWithAttributes(Path path) throws IOException {
        this.path = path;
        Map<String,Object> attrs = Files.readAttributes(path,
                "size,isSymbolicLink,isDirectory,lastModifiedTime",
                LinkOption.NOFOLLOW_LINKS);
        isSymlink = (Boolean) attrs.get("isSymbolicLink");
        if (isSymlink) {
            //symlink to a directory is treated as a directory
            //follow link
            isDirectory = PathUtils.isDirectory(path);
        } else {
            isDirectory = (Boolean) attrs.get("isDirectory");
        }
        FileTime time = (FileTime) attrs.get("lastModifiedTime");
        if (time != null) {
            lastModified = new Date(time.toMillis());
        }
        size = (Long) attrs.get("size");
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public boolean isSymlink() {
        return isSymlink;
    }

    public Long getSize() {
        return size;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public Path getPath() {
        return path;
    }
}

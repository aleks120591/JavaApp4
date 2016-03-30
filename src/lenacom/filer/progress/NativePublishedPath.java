package lenacom.filer.progress;

import lenacom.filer.path.PathUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class NativePublishedPath implements PublishedPath {
    private String name = "";
    private long size = 0;

    public NativePublishedPath(Path base, Path path) {
        name = base.relativize(path).toString();
        if (!PathUtils.isDirectory(path)) {
            try {
                size = Files.size(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public long getSize() {
        return size;
    }

    public String getName() {
        return name;
    }

}

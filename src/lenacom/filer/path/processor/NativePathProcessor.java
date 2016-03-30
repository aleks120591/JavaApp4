package lenacom.filer.path.processor;

import lenacom.filer.path.PathAttributes;
import lenacom.filer.path.PathUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;

public class NativePathProcessor implements PathProcessor {
    private static final NativePathProcessor INSTANCE = new NativePathProcessor();

    public static NativePathProcessor getInstance() {
        return INSTANCE;
    }

    private NativePathProcessor() {
    }

    @Override
    public Long getSize(Path path) throws Exception {
        return Files.size(path);
    }

    @Override
    public Date getLastModified(Path path) throws Exception {
        return PathUtils.getLastModified(path);
    }

    @Override
    public boolean fileExists(Path path) {
        return PathUtils.existsNoFollowLink(path);
    }

    @Override
    public boolean directoryExists(Path path) {
        return PathUtils.existsNoFollowLink(path);
    }

    @Override
    public boolean isDirectory(Path path) throws Exception {
        return PathUtils.isDirectory(path);
    }

    @Override
    public boolean isReadonly(Path path) {
        return PathAttributes.isReadonly(path);
    }
}

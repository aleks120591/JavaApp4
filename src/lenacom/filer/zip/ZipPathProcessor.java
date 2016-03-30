package lenacom.filer.zip;

import lenacom.filer.path.processor.PathProcessor;

import java.nio.file.Path;
import java.util.Date;
import java.util.zip.ZipEntry;

public class ZipPathProcessor implements PathProcessor {
    private ZipModel model;

    ZipPathProcessor(ZipModel model) {
        this.model = model;
    }

    @Override
    public Long getSize(Path path) {
        ZipEntry entry = model.getEntry(path);
        if (entry != null && !entry.isDirectory()) return entry.getSize();
        return model.getDirectorySize(path);
    }

    @Override
    public Date getLastModified(Path path) {
        ZipEntry entry = model.getEntry(path);
        return new Date(entry == null? 0 : entry.getTime());
    }

    @Override
    public boolean isDirectory(Path path) {
        return model.getChildren(path) != null;
    }

    @Override
    public boolean isReadonly(Path path) {
        return false;
    }

    @Override
    public boolean directoryExists(Path path) {
        return model.getChildren(path) != null;
    }

    @Override
    public boolean fileExists(Path path) {
        ZipEntry entry = model.getEntry(path);
        return entry != null && !entry.isDirectory();
    }
}
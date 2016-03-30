package lenacom.filer.zip;

import lenacom.filer.progress.PublishedPath;

import java.util.zip.ZipEntry;

class ZipPublishedPath implements PublishedPath {
    private String name = "";
    private long size = 0;

    public ZipPublishedPath(ZipEntry entry) {
        name = entry.getName();
        if (!entry.isDirectory()) {
            size = entry.getSize();
        }
        if (size < 0) size = 0; //size can be -1
    }

    public long getSize() {
        return size;
    }

    public String getName() {
        return name;
    }
}

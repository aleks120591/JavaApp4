package lenacom.filer.zip;

import lenacom.filer.message.Errors;
import lenacom.filer.message.Messages;
import lenacom.filer.path.PathUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.zip.ZipEntry;

//create symlink to zip, then edit zip, link is removed
class ZipUtils {
    //return written bytes
    static int writeEntry(InputStream in, OutputStream os) throws IOException {
        byte[] buffer = new byte[8192];
        int read;
        int written = 0;
        while (-1 != (read = in.read(buffer))) {
            os.write(buffer, 0, read);
            written += read;
        }
        return written;
    }

    static Path createTempZip(ZipModel model) throws IOException {
        Path zip = model.getZip();
        if (PathUtils.existsNoFollowLink(zip) && !Files.isWritable(zip)) {
            Messages.showMessage("err.zip.not.writable", zip);
            return null;
        }
        return PathUtils.createTempFileInDirectory(zip.getParent());
    }

    static void replaceZipWithTempZip(ZipModel model, Path tmpZip) throws IOException {
        Path zip = model.getZip();
        if (Files.isSymbolicLink(zip)) zip = Files.readSymbolicLink(zip);
        Files.move(tmpZip, zip, StandardCopyOption.REPLACE_EXISTING);
    }

    static void unlockAndDeleteTempZip(ZipModel model, Path tmpZip) {
        ZipLocker.unlock(model.getZip());
        if (tmpZip != null) {
            try {
                Files.deleteIfExists(tmpZip);
            } catch (IOException e) {
                Errors.showError(e);
            }
        }
    }

    static long getSize(ZipModel model, PathFilter filter) {
        return new ZipSize(model, filter).getSize();
    }

    private static class ZipSize {
        private ZipModel model;
        private PathFilter filter;

        ZipSize(ZipModel model, PathFilter filter) {
            this.model = model;
            this.filter = filter;
        }

        long getSize() {
            Set<Path> children = model.getChildren(model.getZip());
            long size = 0;
            for (Path path: children) {
                size += getSize(path);
            }
            return size;
        }

        private long getSize(Path parent) {
            if (filter != null && !filter.include(model.toRelativeZipPath(parent))) return 0;
            Set<Path> children = model.getChildren(parent);
            if (children == null) return 0;
            long size = 0;
            for (Path child: children) {
                if (filter != null && !filter.include(model.toRelativeZipPath(child))) continue;
                ZipEntry entry = model.getEntry(child);
                if (entry != null && !entry.isDirectory()) {
                    size += entry.getSize();
                } else {
                    //it can be dir that doesn't have an entry (the only line on dirs /dir1/dir2/dir3...)
                    size += getSize(child);
                }
            }
            return size;
        }
    }
}

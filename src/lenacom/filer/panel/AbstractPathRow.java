package lenacom.filer.panel;

import lenacom.filer.config.Settings;
import lenacom.filer.path.NativePathWithAttributes;
import lenacom.filer.path.PathAttributes;
import lenacom.filer.path.PathSize;
import lenacom.filer.path.PathUtils;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.DosFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Date;
import java.util.Set;

abstract class AbstractPathRow implements PathRow {
    protected Path path;
    protected String name;
    protected String extension;
    protected PathSize size;
    protected boolean sizeApproximate;
    protected Date date;
    protected String formattedDate;
    protected boolean isSymlink;
    private String attributes = "";
    protected Icon icon;
    private Path symlinkTarget;

    protected void initNativePath(NativePathWithAttributes pwa) throws IOException {
        this.path = pwa.getPath();
        this.isSymlink = pwa.isSymlink();
        if (isSymlink) {
            this.size = PathSizePlaceholder.forLink();
        }

        this.date = pwa.getLastModified();

        if (Settings.isShowAttributes()) {
            initAttributes();
        }
    }

    private void initAttributes() {
        DosFileAttributes dosAttributes = PathAttributes.getDosAttributes(path);
        if (dosAttributes != null) {
            char[] a = new char[4];
            a[0] = dosAttributes.isArchive()? 'a' : '-';
            a[1] = dosAttributes.isHidden()? 'h' : '-';
            a[2] = dosAttributes.isReadOnly()? 'r' : '-';
            a[3] = dosAttributes.isSystem()? 's' : '-';
            attributes = new String(a);
        } else {
            Set<PosixFilePermission> posixAttributes = PathAttributes.getPosixAttributes(path);
            if (posixAttributes != null) {
                char[] a = new char[9];
                a[0] = posixAttributes.contains(PosixFilePermission.OWNER_READ)? 'r' : '-';
                a[1] = posixAttributes.contains(PosixFilePermission.OWNER_WRITE)? 'w' : '-';
                a[2] = posixAttributes.contains(PosixFilePermission.OWNER_EXECUTE)? 'x' : '-';
                a[3] = posixAttributes.contains(PosixFilePermission.GROUP_READ)? 'r' : '-';
                a[4] = posixAttributes.contains(PosixFilePermission.GROUP_WRITE)? 'w' : '-';
                a[5] = posixAttributes.contains(PosixFilePermission.GROUP_EXECUTE)? 'x' : '-';
                a[6] = posixAttributes.contains(PosixFilePermission.OTHERS_READ)? 'r' : '-';
                a[7] = posixAttributes.contains(PosixFilePermission.OTHERS_WRITE)? 'w' : '-';
                a[8] = posixAttributes.contains(PosixFilePermission.OTHERS_EXECUTE)? 'x' : '-';
                attributes = new String(a);
            }
        }
    }

    protected void initZipPath(Path path) {
        this.path = path;
        this.isSymlink = false;
    }

    @Override
    public boolean isSymlink() {
        return isSymlink;
    }

    @Override
    public Path getPath() {
        return path;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getExtension() {
        return extension;
    }

    @Override
    public PathSize getSize() {
        return size;
    }

    @Override
    public Date getDate() {
        return date;
    }

    @Override
    public String getFormattedDate() {
        if (formattedDate == null) {
            this.formattedDate = PathUtils.formatDate(date);
        }
        return formattedDate;
    }

    @Override
    public String getAttributes() {
        return attributes;
    }

    @Override
    public String toHtml() {
        return name;
    }

    @Override
    public boolean isSizeApproximate() {
        return sizeApproximate;
    }

    @Override
    public Path getSymlinkTarget() {
        if (!isSymlink()) return path;
        if (symlinkTarget == null) {
            try {
                symlinkTarget = Files.readSymbolicLink(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return symlinkTarget;
    }
}

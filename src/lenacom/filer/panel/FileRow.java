package lenacom.filer.panel;

import lenacom.filer.config.Colors;
import lenacom.filer.path.*;
import lenacom.filer.zip.ZipPathProcessor;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.EnumMap;

public class FileRow extends AbstractPathRow {
    private boolean isZip;
    private Color background;

    //we can be creating a FileRow for a file that is already deleted
    FileRow(NativePathWithAttributes pwa) throws IOException {
        initNativePath(pwa);
        assert(!pwa.isDirectory());
        String name = PathUtils.getName(this.path);
        EnumMap<FileExtension, String> result = FileExtension.getFileExtension(name);
        this.name = result.get(FileExtension.NAME);
        this.extension = result.get(FileExtension.EXTENSION);
        if (!isSymlink) {
            this.size = new FormattedPathSize(pwa.getSize());
            this.sizeApproximate = false;
        }
        isZip = PathUtils.isZip(path);
    }

    FileRow(Path path, ZipPathProcessor pp) {
        assert(pp != null && !pp.isDirectory(path));
        initZipPath(path);
        String name = path.getFileName().toString();
        EnumMap<FileExtension, String> result = FileExtension.getFileExtension(name);
        this.name = result.get(FileExtension.NAME);
        this.extension = result.get(FileExtension.EXTENSION);
        this.size = new FormattedPathSize(pp.getSize(path));
        this.sizeApproximate = false;
        this.date = pp.getLastModified(path);
        this.formattedDate = PathUtils.formatDate(date);
        this.isZip = PathUtils.getZipExtension().equalsIgnoreCase(extension);
    }

    public boolean isZip() {
        return isZip;
    }

    @Override
    public Icon getIcon() {
        if (icon == null) {
            icon = PathIcons.getFileIcon(path);
        }
        return icon;
    }

    public Color getBackground() {
        if (background == null) {
            background = Colors.getBackgroundByExtension(extension);
        }
        return background;
    }

}

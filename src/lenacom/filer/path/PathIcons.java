package lenacom.filer.path;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.concurrent.ConcurrentHashMap;

public class PathIcons {
    private static final FileSystemView view = FileSystemView.getFileSystemView();
    private static final ConcurrentHashMap<String, Icon> defaultIcons = new ConcurrentHashMap<>();
    private static Icon defaultDirectoryIcon;

    public static Icon getFileIcon(Path path) {
        Icon icon = view.getSystemIcon(path.toFile());
        if (icon == null && !PathUtils.existsNoFollowLink(path)/*inside zip*/) {
            icon = getDefaultFileIcon(path);
        }
        return icon;
    }

    public static Icon getDirectoryIcon(Path path) {
        Icon icon = view.getSystemIcon(path.toFile());
        if (icon == null && !PathUtils.existsNoFollowLink(path)/*inside zip*/) {
            icon = getDefaultDirectoryIcon();
        }
        return icon;
    }

    private static Icon getDefaultFileIcon(Path path) {
        EnumMap<FileExtension, String> fileExtension = FileExtension.getFileExtension(PathUtils.getName(path));
        String extension = fileExtension.get(FileExtension.EXTENSION);
        Icon defaultIcon = defaultIcons.get(extension);
        if (defaultIcon == null) {
            try {
                Path tmpFile = PathUtils.createTempFile("." + extension);
                defaultIcon = view.getSystemIcon(tmpFile.toFile());
                defaultIcons.put(extension, defaultIcon);
                Files.deleteIfExists(tmpFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return defaultIcon;
    }

    private static Icon getDefaultDirectoryIcon() {
        if (defaultDirectoryIcon == null) {
            try {
                Path tmpDir = PathUtils.createTempDirectory();
                defaultDirectoryIcon = view.getSystemIcon(tmpDir.toFile());
                Files.deleteIfExists(tmpDir);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return defaultDirectoryIcon;
    }
}

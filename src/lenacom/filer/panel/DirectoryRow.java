package lenacom.filer.panel;

import lenacom.filer.path.*;
import lenacom.filer.zip.ZipPathProcessor;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class DirectoryRow extends AbstractPathRow {
    //setting a directory size as an attribute will cause a path modified event, the row will be recreated
    private static final ConcurrentHashMap<Path, Long> sizeUpdatedTime = new ConcurrentHashMap<>();
    private static final int VALID_SIZE_INTERVAL_MILLIS = 10 * 1000; //10 sec
    private List<DirectoryRowListener> listeners = new ArrayList<>();

    //we can be creating a DirectoryRow for a directory that is already deleted
    DirectoryRow(NativePathWithAttributes pwa) throws IOException {
        initNativePath(pwa);
        assert(pwa.isDirectory() || PathUtils.isZip(this.path) || !PathUtils.existsFollowLink(this.path));
        this.name = PathUtils.getName(this.path);
        this.extension = "";
        if (!isSymlink) {
            Long size = PathAttributes.getDirectorySize(path);
            if (size != null) {
                Long time = sizeUpdatedTime.get(path);
                this.sizeApproximate = time == null || (System.currentTimeMillis() - time) > VALID_SIZE_INTERVAL_MILLIS;
                this.size = new FormattedPathSize(size);
            } else {
                this.size = PathSizePlaceholder.forDirectory();
            }
        }
    }

    DirectoryRow(Path path, ZipPathProcessor pp) {
        assert(pp != null && pp.isDirectory(path));
        initZipPath(path);
        this.name = PathUtils.getName(path);
        this.extension = "";

        Long size = pp.getSize(path);
        this.size = size != null? new FormattedPathSize(size) : PathSizePlaceholder.forDirectory();
        this.sizeApproximate = false;

        this.date = pp.getLastModified(path);
        this.formattedDate = PathUtils.formatDate(date);
    }

    public void setSize(long bytes) {
        this.size = new FormattedPathSize(bytes);
        this.sizeApproximate = false;
        sizeUpdatedTime.put(path, System.currentTimeMillis());
        for (DirectoryRowListener l: listeners) l.sizeModified(this);
    }

    public void addListener(DirectoryRowListener l) {
        listeners.add(l);
    }

    public void removeListener(DirectoryRowListener l) {
        listeners.remove(l);
    }

    public boolean canCalculateSize() {
        return !isSymlink;
    }

    @Override
    public Icon getIcon() {
        if (icon == null) {
            icon = PathIcons.getDirectoryIcon(path);
        }
        return icon;
    }
}


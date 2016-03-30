package lenacom.filer.panel;

import java.nio.file.Path;

public interface XTableListener {
    void workingDirectoryChanged(XTableContext path);

    void pathCreated(Path path);

    void pathDeleted(Path path);

    void pathModified(Path path);

    void selectionChanged(PathRow[] rows);

    void startWaiting();
}

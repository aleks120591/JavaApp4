package lenacom.filer.action.find;

import java.nio.file.Path;

class ProcessedPath {
    protected Path path;

    ProcessedPath(Path path) {
        this.path = path;
    }

    Path getPath() {
        return path;
    }
}
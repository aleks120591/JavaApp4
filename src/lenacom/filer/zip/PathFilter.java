package lenacom.filer.zip;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

abstract class PathFilter {
    protected final Set<Path> selectedPaths;

    PathFilter(Path[] selectedPaths) {
        this.selectedPaths = new HashSet<>();
        for (Path path: selectedPaths) {
            this.selectedPaths.add(path);
        }
    }

    void addPath(Path path) {
        assert(!path.isAbsolute());
        selectedPaths.add(path);
    }

    abstract boolean include(Path testPath);
}

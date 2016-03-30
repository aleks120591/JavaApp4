package lenacom.filer.zip;

import java.nio.file.Path;
import java.util.Map;

class ZipBuilderParameters {
    private ZipModel model;
    private ExcludePathFilter filter;
    private Path[] pathsToAdd;
    private Map<Path, Path> sourceToTarget;
    private Path targetDirectory;
    private boolean deleteAddedPaths = false;

    ZipBuilderParameters(ZipModel model) {
        this.model = model;
    }

    ZipModel getModel() {
        return model;
    }

    ExcludePathFilter getFilter() {
        return filter;
    }

    void setFilter(ExcludePathFilter filter) {
        this.filter = filter;
    }

    Path[] getPathsToAdd() {
        return pathsToAdd;
    }

    void setPathsToAdd(Path[] pathsToAdd) {
        assert(pathsToAdd.length > 0);
        for (Path path: pathsToAdd) assert(path.isAbsolute());
        this.pathsToAdd = pathsToAdd;
    }

    Map<Path, Path> getSourceToTarget() {
        return sourceToTarget;
    }

    void setSourceToTarget(Map<Path, Path> sourceToTarget) {
        for (Map.Entry<Path, Path> entry : sourceToTarget.entrySet()) {
            assert(!entry.getValue().isAbsolute());
        }
        this.sourceToTarget = sourceToTarget;
    }

    boolean isDeleteAddedPaths() {
        return deleteAddedPaths;
    }

    void setDeleteAddedPaths(boolean deleteAddedPaths) {
        this.deleteAddedPaths = deleteAddedPaths;
    }

    Path getTargetDirectory() {
        return targetDirectory;
    }

    void setTargetDirectory(Path targetDirectory) {
        assert(targetDirectory == null || !targetDirectory.isAbsolute());
        this.targetDirectory = targetDirectory;
    }
}

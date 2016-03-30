package lenacom.filer.action.find;

import lenacom.filer.message.Errors;

import java.nio.file.Path;
import java.util.List;

class FindInFoundWorker extends AbstractFindWorker {
    private List<FoundPath> paths;

    FindInFoundWorker(FindDialog dialog, List<FoundPath> paths, Path base, FindParameters params) {
        super(dialog, base, params);
        this.paths = paths;
    }

    @Override
    protected Void doInBackground() {
        try {
            tryToDoInBackground();
        } catch (Exception e) {
            Errors.showError(e);
        }
        return null;
    }

    private void tryToDoInBackground() throws Exception {
        for (FoundPath foundPath: paths) {
            Path resolvedPath = base.resolve(foundPath.getPath());
            if (foundPath instanceof FoundFile) {
                FoundFile foundFile = (FoundFile) foundPath;
                publishFile(resolvedPath, foundFile.getSize());
            } else {
                publishDirectory(resolvedPath);
            }
        }
    }
}

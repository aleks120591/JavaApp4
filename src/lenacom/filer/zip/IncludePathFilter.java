package lenacom.filer.zip;

import java.nio.file.Path;

//this filter is usually created in the event thread and then is read in a background thread started later
//it's not necessary to synchronize the filter
final class IncludePathFilter extends PathFilter {
    IncludePathFilter(Path[] selectedPaths) {
        super(selectedPaths);
    }

    @Override
    public boolean include(Path testPath) {
        assert(!testPath.isAbsolute());
        for (Path selectedPath : selectedPaths) {
            if (testPath.startsWith(selectedPath)) {
                return true;
            }
        }
        return false;
    }
}

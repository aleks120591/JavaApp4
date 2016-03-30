package lenacom.filer.zip;

import java.nio.file.Path;

//this filter is usually created in the event thread and then is read/updated in a background thread started later
//it's not necessary to synchronize the filter
final class ExcludePathFilter extends PathFilter {
    ExcludePathFilter(Path[] selectedPaths) {
        super(selectedPaths);
    }

    @Override
    public boolean include(Path testPath) {
        assert(!testPath.isAbsolute());
        for (Path selectedPath : selectedPaths) {
            if (testPath.startsWith(selectedPath)) {
                return false;
            }
        }
        return true;
    }
}

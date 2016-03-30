package lenacom.filer.action.find;

import lenacom.filer.path.PathUtils;

import javax.swing.*;
import java.nio.file.Path;

class FoundDirectory extends FoundPath {

    FoundDirectory(Path path, Icon icon) {
        super(path);
        setIcon(icon);
    }

    @Override
    String getName() {
        if (name == null) name = PathUtils.getName(path);
        return name;
    }

    void refresh() {
        //do nothing
    }
}

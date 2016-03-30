package lenacom.filer.panel;

import lenacom.filer.util.BackForwardStack;

import java.nio.file.Path;

public class PathStack extends BackForwardStack<Path> {
    private XTableModel model;

    PathStack(XTableModel model) {
        this.model = model;
    }

    public void activate(Path path) {
        model.setContextDirectory(path);
    }
}

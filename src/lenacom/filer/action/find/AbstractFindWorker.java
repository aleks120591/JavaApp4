package lenacom.filer.action.find;

import lenacom.filer.config.Configuration;
import lenacom.filer.config.Constants;
import lenacom.filer.message.Messages;
import lenacom.filer.path.PathIcons;

import javax.swing.*;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.List;

abstract class AbstractFindWorker extends SwingWorker<Void, ProcessedPath> {
    private FindDialog dialog;
    protected Path base;
    private PathMatcher pathMatcher;
    private Finder finder;
    private boolean limitedSize = false;
    private long maxSize;

    AbstractFindWorker(FindDialog dialog, Path base, FindParameters params) {
        this.dialog = dialog;
        this.base = base;
        String searchString = ("glob:**" + File.separatorChar).replace("\\", "\\\\") + params.getName();
        pathMatcher = params.getName() == null? null :
                FileSystems.getDefault().getPathMatcher(searchString);
        if (params.getContains() != null) {
            finder = new Finder(params.getContains(),
                    params.isCaseSensitive(), params.isAllExtracts());
            limitedSize = Configuration.getBoolean(Configuration.FIND_LIMITED_SIZE, false);
            maxSize = Configuration.getLong(Configuration.TEXT_FILE_MAX_SIZE, Constants.DEFAULT_TEXT_FILE_MAX_SIZE);
        }
    }

    @Override
    protected void process(List<ProcessedPath> processedPaths) {
        List<FoundPath> foundPaths = new ArrayList<>();
        boolean currentPathFound = false;
        for (ProcessedPath processedPath: processedPaths) {
            if (processedPath instanceof FoundPath) {
                foundPaths.add((FoundPath) processedPath);
            } else if (!currentPathFound) {
                currentPathFound = true;
                if (!isCancelled()) {
                    dialog.currentPath(processedPath.getPath());
                }
            }
        }
        if (foundPaths.size() > 0 && !isCancelled()) dialog.found(foundPaths);
    }

    //we publish currently a processed path along with found paths
    protected void publishFile(Path path, long size) {
        assert(path.isAbsolute());
        if (limitedSize && size > maxSize) {
            return;
        }
        Path relativizedPath = base.relativize(path);
        publish(new ProcessedPath(relativizedPath));
        if (pathMatcher == null || pathMatcher.matches(path)) {
            if (finder == null) {
                //searching only by name
                //can be a file or a directory
                assert(!SwingUtilities.isEventDispatchThread());
                Icon icon = PathIcons.getFileIcon(path);
                publish(new FoundFile(relativizedPath, icon, size));
            } else {
                //searching a text inside a file
                try {
                    List<ExtractDetails> extracts = finder.find(path);
                    if (extracts.size() > 0) {
                        assert(!SwingUtilities.isEventDispatchThread());
                        Icon icon = PathIcons.getFileIcon(path);
                        FoundFile foundFile = new FoundFile(relativizedPath, icon, size, extracts);
                        publish(foundFile);
                    }
                } catch (Finder.NoNeedleException e) {
                    Messages.showMessage(dialog, "err.finder.no.needle", e.getContains(), e.getPath());
                    this.cancel(false);
                    dialog.done();
                }
            }
        }
    }

    //we publish currently a processed path along with found paths
    protected void publishDirectory(Path path) {
        assert(path.isAbsolute());
        Path relativizedPath = base.relativize(path);
        publish(new ProcessedPath(relativizedPath));
        assert(pathMatcher != null && finder == null);
        //searching directories only by name
        if (pathMatcher.matches(path)) {
            assert(!SwingUtilities.isEventDispatchThread());
            Icon icon = PathIcons.getFileIcon(path);
            publish(new FoundDirectory(relativizedPath, icon));
        }
    }

    @Override
    protected void done() {
        if (!isCancelled()) dialog.done();
    }
}

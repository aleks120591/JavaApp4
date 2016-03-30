package lenacom.filer.path.processor;

import lenacom.filer.component.MultiChoiceDialog;
import lenacom.filer.config.ResourceKey;
import lenacom.filer.path.FormattedPathSize;
import lenacom.filer.path.PathUtils;
import lenacom.filer.progress.PathProgress;
import lenacom.filer.root.RootFrame;

import javax.swing.*;
import java.nio.file.Path;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class OverwriteProcessor extends SafeProcessor {
    protected static enum OverwritePolicy {
        PROMPT, OVERWRITE_ALL, OVERWRITE_ALL_OLDER, SKIP_ALL
    }
    //this field is accessed from different threads
    private volatile OverwritePolicy overwritePolicy = OverwritePolicy.PROMPT;
    private PathProcessor spp;
    private PathProcessor tpp;
    private PathProgress progress;

    public OverwriteProcessor(PathProcessor processedSource, PathProcessor processedTarget, PathProgress progress) {
        this.spp = processedSource;
        this.tpp = processedTarget;
        this.progress = progress;
    }

    protected void setOverwritePolicy(OverwritePolicy overwritePolicy) {
        this.overwritePolicy = overwritePolicy;
    }

    public void checkAndProcess(Path source) throws Exception {
        Path target = getTarget(source);
        if (spp.isDirectory(source)) {
            if (!tpp.directoryExists(target)) {
                processDirectory(source, target);
            }
        } else {
            if (tpp.fileExists(target)) {
                switch (overwritePolicy) {
                    case PROMPT:
                        prompt(source, target);
                        break;
                    case OVERWRITE_ALL_OLDER:
                        overwriteIfOlder(source, target);
                        break;
                    case OVERWRITE_ALL:
                        overwriteFile(source, target);
                        break;
                    case SKIP_ALL:
                        progress.skipValue(spp.getSize(source));
                        break;
                }
            } else {
                processFile(source, target);
            }
        }
    }

    private void prompt(Path source, final Path target) throws Exception {
        progress.getTimeRecorder().stopRecordingTime();
        final AtomicBoolean process = new AtomicBoolean(false);
        final AtomicBoolean rename = new AtomicBoolean(false);
        final ConcurrentHashMap<Path, Boolean> concurrentRenamedTarget = new ConcurrentHashMap<>();
        Path renamedTarget = null;

        Action overwriteAction = new SafeAction(this, "process.btn.overwrite") {
            @Override
            public void safeActionPerformed() {
                process.set(true);
            }
        };
        Action overwriteAllAction = new SafeAction(this, "process.btn.overwrite.all") {
            @Override
            public void safeActionPerformed() {
                overwritePolicy = OverwritePolicy.OVERWRITE_ALL;
            }
        };
        Action overwriteAllOlderAction = new SafeAction(this, "process.btn.overwrite.all.older") {
            @Override
            public void safeActionPerformed() {
                overwritePolicy = OverwritePolicy.OVERWRITE_ALL_OLDER;
            }
        };
        Action skipAction = new DoNothingAction("process.btn.skip");
        Action skipAllAction = new SafeAction(this, "process.btn.skip.all") {
            @Override
            public void safeActionPerformed() {
                overwritePolicy = OverwritePolicy.SKIP_ALL;
            }
        };
        Action renameAction = new SafeAction(this, "process.btn.rename") {
            @Override
            public void safeActionPerformed() {
                rename.set(true);
                RenameDialog dlg = new RenameDialog(RootFrame.getRoot(), tpp, target);
                dlg.setVisibleRelativeToParent(0.5, null);
                Path renamedTarget = dlg.getNewPath();
                if (renamedTarget != null) {
                    concurrentRenamedTarget.put(renamedTarget, Boolean.TRUE);
                }
            }
        };
        Action stopAction = new SafeAction(this, "process.btn.stop") {
            @Override
            public void safeActionPerformed() {
                cancel();
            }
        };

        final Action[] actions = new Action[]{overwriteAction, overwriteAllAction, overwriteAllOlderAction, skipAction, skipAllAction, renameAction, stopAction};

        final ResourceKey keyTitle = new ResourceKey("process.dlg.confirm.overwrite.title");
        final ResourceKey keyDescr = new ResourceKey("process.dlg.confirm.overwrite", getFileDescription(target, tpp), getFileDescription(source, spp));

        boolean chooseAgain;
        do {
            rename.set(false);
            chooseAgain = false;
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    //run in the event thread
                    MultiChoiceDialog dlg = new MultiChoiceDialog(RootFrame.getRoot(), keyTitle, keyDescr, actions);
                    dlg.setVisibleRelativeToParent();
                }
            });

            if (rename.get()) {
                if (concurrentRenamedTarget.keySet().isEmpty()) {
                    chooseAgain = true;
                } else {
                    renamedTarget = concurrentRenamedTarget.keySet().iterator().next();
                    process.set(true);
                    //run in a background thread
                    //we check if a new path already exists in RenameDialog
                }
            }
        } while (chooseAgain);

        progress.getTimeRecorder().startRecordingTime();

        //we processFile the file in the background thread, not the event thread
        Path processedTarget = renamedTarget != null? renamedTarget : target;
        if (process.get() || overwritePolicy == OverwritePolicy.OVERWRITE_ALL) {
            overwriteFile(source, processedTarget);
        } else if (overwritePolicy == OverwritePolicy.OVERWRITE_ALL_OLDER) {
            overwriteIfOlder(source, processedTarget);
        } else {
            progress.skipValue(spp.getSize(source));
        }
    }

    private void overwriteIfOlder(Path source, Path target) throws Exception {
        boolean isOlder;
        Date sourceDate = spp.getLastModified(source);
        Date targetDate = tpp.getLastModified(target);
        isOlder = sourceDate.compareTo(targetDate) == 1;
        if (isOlder) overwriteFile(source, target);
        else {
            progress.skipValue(spp.getSize(source));
        }
    }

    private String getFileDescription(Path file, PathProcessor pp) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("\"").append(file.getFileName()).append("\"<br>");
        Date date = pp.getLastModified(file);
        String formattedDate = PathUtils.formatDate(date);
        sb.append(formattedDate);
        FormattedPathSize pathSize = new FormattedPathSize(pp.getSize(file));
        sb.append(", ").append(pathSize.toStringWithBytes());
        return sb.toString();
    }

    protected abstract Path getTarget(Path source);
    protected abstract void overwriteFile(Path source, Path target) throws Exception;
    protected abstract void processFile(Path source, Path target) throws Exception;
    protected abstract void processDirectory(Path source, Path target) throws Exception;
}

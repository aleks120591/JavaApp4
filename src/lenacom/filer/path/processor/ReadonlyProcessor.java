package lenacom.filer.path.processor;

import lenacom.filer.component.MultiChoiceDialog;
import lenacom.filer.config.ResourceKey;
import lenacom.filer.config.Resources;
import lenacom.filer.path.PathAttributes;
import lenacom.filer.progress.PathProgress;
import lenacom.filer.root.RootFrame;

import javax.swing.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class ReadonlyProcessor {
    private static enum ProcessReadonlyPolicy {
        PROMPT, PROCESS_ALL_READONLY, SKIP_ALL_READONLY
    }
    //this field is accessed from different threads
    private volatile ProcessReadonlyPolicy readonlyPolicy = ProcessReadonlyPolicy.PROMPT;
    protected final SafePathVisitor visitor;
    private final String processName;
    private PathProgress progress;

    public ReadonlyProcessor(SafePathVisitor visitor, PathProgress progress, String keyProcessName) {
        this.visitor = visitor;
        this.progress = progress;
        this.processName = Resources.getMessage(keyProcessName);
    }

    public void checkAndProcess(Path path) throws Exception {
        if (PathAttributes.isReadonly(path)) {
            if (readonlyPolicy == ProcessReadonlyPolicy.SKIP_ALL_READONLY) {
                progress.skipValue(Files.size(path));
                return;
            }
            if (readonlyPolicy == ProcessReadonlyPolicy.PROCESS_ALL_READONLY) {
                processReadonly(path);
            } else {
                prompt(path);
            }
        } else {
            process(path);
        }
    }

    private void prompt(final Path path) throws Exception {
        progress.getTimeRecorder().stopRecordingTime();
        final AtomicBoolean process = new AtomicBoolean(false);
        final Action processAction = new SafeAction(visitor, processName) {
            @Override
            protected void safeActionPerformed() {
                process.set(true);
            }
        };
        final Action processAllReadonlyAction = new SafeAction(visitor, "process.btn.process.all.readonly", processName) {
            @Override
            protected void safeActionPerformed() {
                readonlyPolicy = ProcessReadonlyPolicy.PROCESS_ALL_READONLY;
                process.set(true);
            }
        };
        final Action skipAction = new DoNothingAction("process.btn.skip");
        final Action skipAllReadonlyAction = new SafeAction(visitor, "process.btn.skip.all.readonly") {
            @Override
            public void safeActionPerformed() {
                readonlyPolicy = ProcessReadonlyPolicy.SKIP_ALL_READONLY;
            }
        };
        final Action stopAction = new SafeAction(visitor, "process.btn.stop") {
            @Override
            public void safeActionPerformed() {
                visitor.cancel();
            }
        };

        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                MultiChoiceDialog dlg = new MultiChoiceDialog(
                        RootFrame.getRoot(),
                        new ResourceKey("confirm.title"),
                        new ResourceKey("process.dlg.confirm.process.readonly", path, processName),
                        processAction, processAllReadonlyAction, skipAction, skipAllReadonlyAction, stopAction
                );
                dlg.setVisibleRelativeToParent();
            }
        });

        progress.getTimeRecorder().startRecordingTime();
        //we will move the path in the background thread, not the event thread
        if (process.get()) processReadonly(path);
        else progress.skipValue(Files.size(path));
    }

    protected abstract void process(Path path) throws Exception;
    protected abstract void processReadonly(Path path) throws Exception;
}

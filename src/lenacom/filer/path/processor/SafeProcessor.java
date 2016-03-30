package lenacom.filer.path.processor;

import lenacom.filer.component.MultiChoiceDialog;
import lenacom.filer.config.ResourceKey;
import lenacom.filer.config.Resources;
import lenacom.filer.root.RootFrame;
import lenacom.filer.util.BasicAction;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicBoolean;

abstract class SafeProcessor {
    //return true if the option 'retry' is selected
    protected final boolean processError(Exception e) {
        final AtomicBoolean returnValue = new AtomicBoolean(false);
        final Action retryAction = new BasicAction("process.btn.retry") {
            @Override
            public void actionPerformed(ActionEvent e) {
                returnValue.set(true);
            }
        };
        final Action goAction = new DoNothingAction("process.btn.continue");
        final Action stopAction = new BasicAction("process.btn.stop") {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancel();
            }
        };
        String message = e.getMessage();
        if (message == null || message.isEmpty()) {
            message = e.getClass().toString();
        }
        final ResourceKey key = new ResourceKey(message);
        e.printStackTrace();

        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    MultiChoiceDialog dlg = new MultiChoiceDialog(RootFrame.getRoot(), new ResourceKey("error.title"),
                            key, retryAction, goAction, stopAction);
                    dlg.setVisibleRelativeToParent();
                }
            });
        } catch (InterruptedException | InvocationTargetException ex) {
            ex.printStackTrace();
            return false;
        }
        return returnValue.get();
    }

    protected abstract void cancel();
}

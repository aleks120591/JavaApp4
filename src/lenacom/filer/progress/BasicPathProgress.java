package lenacom.filer.progress;

import lenacom.filer.config.ResourceKey;
import lenacom.filer.config.Resources;
import lenacom.filer.path.FormattedPathSize;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.List;

public abstract class BasicPathProgress implements PathProgress {
    private static final int SHOW_PROGRESS_DELAY = 100;
    private static final int SHOW_DIALOG_DELAY = 1000; //1 sec
    private static final String PROCESSED_TEMPLATE = Resources.getMessage("path.progress.processed.template");
    private static final String REMAINING_TIME_TEMPLATE = Resources.getMessage("path.progress.remaining.time.template");
    private PathProgressDialog dlg;
    private final Component owner;
    private final ResourceKey keyTitle;
    private final ResourceKey keyDescription;
    private final TimeRecorder timeRecorder;
    private TimerListener timerListener;
    private Timer showProgressTimer;
    private long totalValue = 0;
    private long processedValue = 0;
    private double speed = 0;
    private final boolean extended;
    private String currentText = "";
    private Timer showDialogTimer;

    public BasicPathProgress(Component owner, ResourceKey keyTitle, ResourceKey keyDescription, long totalValue, boolean extended) {
        this.owner = owner;
        this.keyTitle = keyTitle;
        this.keyDescription = keyDescription;
        this.totalValue = totalValue;
        this.extended = extended;
        if (extended) {
            timerListener = new TimerListener();
            showProgressTimer = new Timer(SHOW_PROGRESS_DELAY, timerListener);
        }
        timeRecorder = new TimeRecorder();
        timeRecorder.startRecordingTime();

        showDialogTimer = new Timer(SHOW_DIALOG_DELAY, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showDialog();
            }
        });
        showDialogTimer.setDelay(100);
        showDialogTimer.setRepeats(true);
        showDialogTimer.start();
    }

    private void showDialogInEventDispatchThread() {
        if (timeRecorder.getTotalTime() > SHOW_DIALOG_DELAY && processedValue < totalValue) {
            dlg = new PathProgressDialog(owner, keyTitle, keyDescription, extended) {
                @Override
                protected boolean onCancel() {
                    BasicPathProgress.this.onCancel();
                    BasicPathProgress.this.close();
                    return true;
                }
            };
            dlg.setCurrentText(currentText);
            dlg.setVisibleRelativeToParent();
            showDialogTimer.stop();
        }
    }

    private void showDialog() {
        if (SwingUtilities.isEventDispatchThread()) {
            showDialogInEventDispatchThread();
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    showDialogInEventDispatchThread();
                }
            });
        }
    }

    @Override
    public TimeRecorder getTimeRecorder() {
        return timeRecorder;
    }

    @Override
    public void beforeProcessing(List<PublishedPath> paths) {
        if (paths.size() == 0) return;
        long size = 0;
        for (PublishedPath path: paths) {
            size += path.getSize();
        }
        beforeProcessing(paths.get(paths.size() - 1).getName(), size);
    }

    private void beforeProcessingInEventDispatchThread(String text, long value) {
        if (dlg != null) {
            dlg.setCurrentText(text);
            if (extended) addProgressExtended(value);
        } else {
            BasicPathProgress.this.currentText = text;
        }
    }

    private void beforeProcessing(final String text, final long value) {
        if (SwingUtilities.isEventDispatchThread()) {
            beforeProcessingInEventDispatchThread(text, value);
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    beforeProcessingInEventDispatchThread(text, value);
                }
            });
        }
    }

    private void afterProcessingInEventDispatchThread(long value) {
        processedValue += value;
        if (dlg == null) return;

        long processedTime = timeRecorder.getTotalTime();
        if (processedTime > 0) {
            speed = (double) processedValue / processedTime;
        }

        int percent = Long.valueOf(Math.round((double) 100 * processedValue / totalValue)).intValue();
        dlg.setProgressPercent(percent);

        dlg.setRemainingTimeText(getRemainingTime());
        dlg.setProcessedText(MessageFormat.format(PROCESSED_TEMPLATE, formatValue(processedValue), formatValue(totalValue)));
    }

    @Override
    public void afterProcessing(final long value) {
        if (SwingUtilities.isEventDispatchThread()) {
            afterProcessingInEventDispatchThread(value);
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    afterProcessingInEventDispatchThread(value);
                }
            });
        }
    }

    private String getRemainingTime() {
        int expectedSeconds = new Double((totalValue - processedValue) / speed / 1000).intValue();
        if (expectedSeconds < 0) expectedSeconds = 0;

        int hours, minutes, seconds;
        hours = expectedSeconds / 60 / 60;
        expectedSeconds = expectedSeconds % (60 * 60);
        minutes = expectedSeconds / 60;
        seconds = expectedSeconds % 60;
        return String.format(REMAINING_TIME_TEMPLATE, hours, minutes, seconds);
    }

    private void addProgressExtended(long value) {
        FileProgressBar fileProgressBar = dlg.getFileProgressBar();
        fileProgressBar.setIndeterminate(speed == 0);
        fileProgressBar.setMaximum(value);
        fileProgressBar.setStringPainted(fileProgressBar.getMaximum() > 0);
        fileProgressBar.setValue(0);
        timerListener.currentValue = 0;
        if (speed > 0 && !showProgressTimer.isRunning()) showProgressTimer.start();
    }

    private final class TimerListener implements ActionListener {
        long currentValue = 0;

        @Override
        public void actionPerformed(ActionEvent e) {
            currentValue += SHOW_PROGRESS_DELAY * speed;
            dlg.getFileProgressBar().setValue(currentValue);
        }
    }

    @Override
    public void skipValue(final long value) {
        if (SwingUtilities.isEventDispatchThread()) {
            totalValue -= value;
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    totalValue -= value;
                }
            });
        }
    }

    private void closeInEventDispatchThread() {
        if (extended && showProgressTimer.isRunning()) showProgressTimer.stop();
        showDialogTimer.stop();
        if (dlg != null) dlg.dispose();
        processedValue = totalValue;
    }

    @Override
    public void close() {
        if (SwingUtilities.isEventDispatchThread()) {
            closeInEventDispatchThread();
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    closeInEventDispatchThread();
                }
            });
        }
    }

    private String formatValue(long value) {
        return new FormattedPathSize(value).toString();
    }

    protected abstract void onCancel();
}

package lenacom.filer.progress;

import javax.swing.*;

class FileProgressBar extends JProgressBar {
    private static final int KB = 1024;

    public void setMaximum(long size) {
        super.setMaximum(Math.round(size / KB));
    }

    public void setValue(long size) {
        int value = Math.round(size / KB);
        super.setValue(Math.min(this.getMaximum(), value));
    }
}

package lenacom.filer.progress;

import lenacom.filer.component.ButtonsDialog;
import lenacom.filer.component.HtmlLabel;
import lenacom.filer.component.UIConstants;
import lenacom.filer.component.XLabel;
import lenacom.filer.config.ResourceKey;

import javax.swing.*;
import java.awt.*;

abstract class PathProgressDialog extends ButtonsDialog {
    private static final double WIDTH_PERCENT = 0.5;
    private static final String EMPTY = " ";
    private JLabel lblCurrent, lblProcessed, lblRemainingTime;
    private FileProgressBar fileProgressBar;
    private JProgressBar progressBar;
    private HtmlLabel lblDescription;

    PathProgressDialog(Component owner, ResourceKey keyTitle, ResourceKey keyDescription, boolean extended) {
        super(owner, keyTitle);
        this.setCenterComponent(getCenterPanel(keyDescription, extended));
        addRightButtons(this.createCancelButton());
        pack();
        setModal(false);
    }

    private JPanel getCenterPanel(ResourceKey keyDescription, boolean extended) {
        lblDescription = new HtmlLabel(keyDescription);
        lblCurrent = new JLabel(EMPTY);
        lblProcessed = new JLabel(EMPTY);
        lblRemainingTime = new JLabel(EMPTY);

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        if (extended) {
            fileProgressBar = new FileProgressBar();
            fileProgressBar.setMinimum(0);
            fileProgressBar.setStringPainted(true);
        }

        JPanel pnlCenter = new JPanel(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 0;
        constraints.weighty = 0;
        constraints.gridwidth = 2;
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(0, 0, 0, 0);
        pnlCenter.add(lblDescription, constraints);

        constraints.gridy++;
        constraints.insets = new Insets(UIConstants.VERTICAL_GAP, 0, 0, 0);
        pnlCenter.add(lblCurrent, constraints);

        constraints.gridy++;
        constraints.weightx = 1;
        pnlCenter.add(progressBar, constraints);

        if (extended) {
            constraints.gridy++;
            constraints.weightx = 0;
            pnlCenter.add(fileProgressBar, constraints);
        }

        constraints.gridy++;
        constraints.weightx = 0.5;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        pnlCenter.add(lblProcessed, constraints);

        constraints.gridx++;
        constraints.weightx = 0;
        constraints.anchor = GridBagConstraints.NORTHEAST;
        constraints.insets = new Insets(UIConstants.VERTICAL_GAP, UIConstants.HORIZONTAL_GAP, 0, 0);
        pnlCenter.add(lblRemainingTime, constraints);

        return pnlCenter;
    }

    @Override
    public void setVisibleRelativeTo(Component relativeTo) {
        super.setVisibleRelativeTo(relativeTo, WIDTH_PERCENT, null);
    }

    @Override
    public void setVisibleRelativeToParent() {
        super.setVisibleRelativeToParent(WIDTH_PERCENT, null);
    }

    void setCurrentText(String text) {
        lblCurrent.setText(text);
    }

    void setProcessedText(String text) {
        lblProcessed.setText(text);
    }

    void setRemainingTimeText(String text) {
        lblRemainingTime.setText(text);
    }

    void setProgressPercent(int percent) {
        progressBar.setValue(percent);
    }

    FileProgressBar getFileProgressBar() {
        return fileProgressBar;
    }

    @Override
    public void pack() {
        super.pack();
        int diff = lblDescription.adjustHeight();
        this.setHeight(getHeight() + diff);
    }
}

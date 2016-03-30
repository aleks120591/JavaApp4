package lenacom.filer.panel;

import lenacom.filer.component.UIConstants;
import lenacom.filer.component.XLabel;
import lenacom.filer.config.Resources;
import lenacom.filer.path.FormattedPathSize;
import lenacom.filer.path.PathUtils;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;
import java.text.MessageFormat;

public class FooterPanel extends JPanel implements XTableListener {
    private XTable xtbl;
    private JLabel lblFreeSpace = new JLabel();
    private JLabel lblCountSelectedDirs = new JLabel();
    private JLabel lblCountSelectedFiles = new JLabel();
    private JLabel lblCountSymlinks = new JLabel();
    private JLabel lblSelectedSize = new JLabel();

    private static final String FREE_TOOLTIP_TEMPLATE = Resources.getMessage("footer.free.tooltip.template");
    private static final String SELECTED_TOOLTIP_TEMPLATE = Resources.getMessage("footer.selected.tooltip.template");

    FooterPanel(XTable xtbl) {
        xtbl.addListener(this);
        this.xtbl = xtbl;

        this.setLayout(new FlowLayout(FlowLayout.LEFT, UIConstants.HORIZONTAL_GAP, UIConstants.VERTICAL_GAP));
        this.add(XLabel.createWithColon("footer.free"));
        this.add(lblFreeSpace);
        this.add(XLabel.createWithColon("footer.selected"));
        this.add(lblSelectedSize);
        this.add(XLabel.createWithColon("footer.files"));
        this.add(lblCountSelectedFiles);
        this.add(XLabel.createWithColon("footer.dirs"));
        this.add(lblCountSelectedDirs);
        if (PathUtils.isSymlinkSupported()) {
            this.add(XLabel.createWithColon("footer.symlinks"));
            this.add(lblCountSymlinks);
        }

        refresh();
    }

    public void refresh() {
        updateFreeSpace();
        updateSelected();
    }

    private void updateFreeSpace() {
        Path path = xtbl.getContext().getDirectory();
        if (!PathUtils.existsNoFollowLink(path)) {
            path = PathUtils.getClosestExistentParent(path);
        }
        long freeSpace = path.toFile().getFreeSpace();
        FormattedPathSize pathSize = new FormattedPathSize(freeSpace);
        lblFreeSpace.setText(pathSize.toString());
        String tooltip = MessageFormat.format(FREE_TOOLTIP_TEMPLATE, pathSize.toStringWithBytes());
        lblFreeSpace.setToolTipText(tooltip);
    }

    private void updateSelected() {
        PathRow[] selectedRows = xtbl.getSelectedRows();
        updateSelected(selectedRows);
    }

    private void updateSelected(PathRow[] selectedRows) {
        long size = 0;
        int countFiles = 0;
        int countDirs = 0;
        int countSymlinks = 0;
        boolean sizeApproximate = false;
        for (PathRow row: selectedRows) {
            if (row.isSymlink()) {
                countSymlinks++;
            } else {
                if (row instanceof DirectoryRow) {
                    countDirs++;
                    if (!sizeApproximate && row.isSizeApproximate()) {
                        sizeApproximate = true;
                    }
                } else {
                    countFiles++;
                }
                if (row.getSize() != null) {
                    size += row.getSize().getBytes();
                }
            }
        }
        lblSelectedSize.setForeground(sizeApproximate? Color.GRAY : Color.BLACK);
        FormattedPathSize pathSize = new FormattedPathSize(size);
        lblSelectedSize.setText(pathSize.toString());
        String tooltip = MessageFormat.format(SELECTED_TOOLTIP_TEMPLATE, pathSize.toStringWithBytes());
        lblSelectedSize.setToolTipText(tooltip);
        lblCountSelectedFiles.setText(String.valueOf(countFiles));
        lblCountSelectedDirs.setText(String.valueOf(countDirs));
        lblCountSymlinks.setText(String.valueOf(countSymlinks));
    }

    private void update() {
        updateFreeSpace();
        updateSelected();
    }

    @Override
    public void workingDirectoryChanged(XTableContext path) {
        update();
    }

    @Override
    public void pathCreated(Path path) {
        update();
    }

    @Override
    public void pathDeleted(Path path) {
        update();
    }

    @Override
    public void pathModified(Path path) {
        update();
    }

    @Override
    public void selectionChanged(PathRow[] rows) {
        updateSelected(rows);
    }

    @Override
    public void startWaiting() {
        //do nothing
    }
}

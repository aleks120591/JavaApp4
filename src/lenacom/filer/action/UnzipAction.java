package lenacom.filer.action;

import lenacom.filer.component.*;
import lenacom.filer.message.Errors;
import lenacom.filer.panel.FileRow;
import lenacom.filer.panel.PathRow;
import lenacom.filer.panel.XPanel;
import lenacom.filer.panel.XPanels;
import lenacom.filer.path.FileExtension;
import lenacom.filer.path.PathUtils;
import lenacom.filer.path.processor.PathProcessor;
import lenacom.filer.zip.ZipWorker;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;

class UnzipAction extends XAction implements ContextAction {
    private static final Set<SelectionType> types;

    static {
        types = new HashSet<>();
        types.add(SelectionType.MULTIPLE);
        types.add(SelectionType.ZIP);
    }

    @Override
    public Set<SelectionType> getSelectionTypes() {
        return types;
    }

    public UnzipAction(String key, boolean withEllipsis) {
        super(key, withEllipsis);
    }

    @Override
    public boolean isEnabled() {
        SelectionType type = SelectionType.getSelectionType(getTable());
        if (!types.contains(type)) return false;
        if (getTable().getContext().isZip()) return false;
        PathRow[] rows = getTable().getSelectedRows();
        for (PathRow row: rows) {
            if ((row instanceof FileRow) && ((FileRow) row).isZip()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void act() {
        if (!isEnabled()) return;
        new UnzipDialog(getPanel());
    }

    private class UnzipDialog extends OkCancelDialog {
        private static final int ROWS = 10;
        private XPanel xpnl;
        private Path[] paths;
        private JRadioButton rbCurrentPanel, rbOtherPanel;

        UnzipDialog(XPanel xpnl) {
            super(xpnl, "dlg.unzip.title");
            this.xpnl = xpnl;

            JPanel pnlCenter = new JPanel(new GridBagLayout());
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.gridx = 0;
            constraints.gridy = 0;
            constraints.anchor = GridBagConstraints.NORTHWEST;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.weightx = 0;
            constraints.weighty = 0;

            Path currentDirectory = getDirectory(xpnl);
            Path otherDirectory = getDirectory(XPanels.getOtherPanel(xpnl));
            if (!currentDirectory.equals(otherDirectory)) {
                rbCurrentPanel = new XRadioButton("dlg.unzip.lbl.this.panel", true);
                rbOtherPanel = new XRadioButton("dlg.unzip.lbl.other.panel");
                ButtonGroup bg = new ButtonGroup();
                bg.add(rbCurrentPanel);
                bg.add(rbOtherPanel);

                constraints.gridwidth = 1;
                constraints.insets = new Insets(0, 0, UIConstants.VERTICAL_GAP, 0);
                pnlCenter.add(rbCurrentPanel, constraints);

                constraints.gridx++;
                constraints.insets = new Insets(0, UIConstants.HORIZONTAL_GAP, UIConstants.VERTICAL_GAP, UIConstants.HORIZONTAL_GAP);
                pnlCenter.add(rbOtherPanel, constraints);

                constraints.gridx = 0;
                constraints.gridy++;
            }

            paths = getPaths();
            JPanel pnlPaths = LabelComponentPanel.createVertical("dlg.unzip.lbl.paths", new JScrollPane(getPathsList(paths)));

            constraints.gridwidth = 2;
            constraints.fill = GridBagConstraints.BOTH;
            constraints.weightx = 1;
            constraints.weighty = 1;
            constraints.insets = new Insets(0, 0, 0, 0);
            pnlCenter.add(pnlPaths, constraints);

            this.setCenterComponent(pnlCenter);
            this.setVisibleRelativeToParent();
        }

        private Path[] getPaths() {
            PathRow[] rows = getTable().getSelectedRows();
            List<Path> paths = new ArrayList<>();
            for (PathRow row: rows) {
                if ((row instanceof FileRow) && ((FileRow) row).isZip()) {
                    paths.add(row.getPath());
                }
            }
            return  paths.toArray(new Path[paths.size()]);
        }

        private JList getPathsList(Path[] paths) {
            List<String> names = new ArrayList<>(paths.length);
            for (Path path: paths) names.add(PathUtils.getName(path));
            Collections.sort(names);

            JList<Object> list = new JList<>(names.toArray());
            list.setCellRenderer(new XListCellRenderer<>());
            list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            list.setVisibleRowCount(Math.min(ROWS, paths.length));
            return list;
        }

        private Path getDirectory(XPanel xpnl) {
            return xpnl.getTable().getContext().getDirectory().toAbsolutePath();
        }

        @Override
        protected boolean onOk() {
            XPanel pnlOutput = (rbCurrentPanel == null || rbCurrentPanel.isSelected())? xpnl : XPanels.getOtherPanel(xpnl);
            Path base = getDirectory(pnlOutput);
            PathProcessor processor = pnlOutput.getTable().getContext().getPathProcessor();

            for (Path path: paths) {
                Path zip = path.toAbsolutePath();
                EnumMap<FileExtension, String> fileExtension = FileExtension.getFileExtension(PathUtils.getName(zip));
                String name = fileExtension.get(FileExtension.NAME);
                Path extractTo = base.resolve(name);

                try {
                    if (!processor.directoryExists(extractTo)) {
                        Files.createDirectory(extractTo);
                    }
                    ZipWorker.create(zip).unpackAll(extractTo);
                } catch (IOException e) {
                    Errors.showError(UnzipDialog.this, e);
                }
            }
            return true;
        }
    }
}

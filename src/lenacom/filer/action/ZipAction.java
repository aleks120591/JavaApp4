package lenacom.filer.action;

import lenacom.filer.component.*;
import lenacom.filer.config.XIcon;
import lenacom.filer.message.Errors;
import lenacom.filer.message.Messages;
import lenacom.filer.panel.XPanel;
import lenacom.filer.panel.XPanels;
import lenacom.filer.path.FileExtension;
import lenacom.filer.path.PathUtils;
import lenacom.filer.path.processor.PathProcessor;
import lenacom.filer.zip.ZipWorker;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.List;

class ZipAction extends XAction implements ContextAction {
    private static final Set<SelectionType> types = SelectionType.getAllExceptParentDirectory();

    @Override
    public Set<SelectionType> getSelectionTypes() {
        return types;
    }

    public ZipAction(String key, boolean withEllipsis) {
        super(key, withEllipsis);
        this.setIcon(XIcon.ZIP.getIcon());
    }

    @Override
    public boolean isEnabled() {
        SelectionType type = SelectionType.getSelectionType(getTable());
        if (!types.contains(type)) return false;
        return !getTable().getContext().isZip();
    }

    @Override
    public void act() {
        if (!isEnabled()) return;
        new ZipDialog(getPanel());
    }

    private class ZipDialog extends OkCancelDialog {
        private static final int ROWS = 10;
        private XPanel xpnl;
        private Path[] paths;
        private XTextField tfName;
        private JRadioButton rbCurrentPanel, rbOtherPanel;

        ZipDialog(XPanel xpnl) {
            super(xpnl, "dlg.zip.title");
            this.xpnl = xpnl;

            paths = xpnl.getTable().getSelectedPaths();

            JPanel pnlCenter = new JPanel(new GridBagLayout());
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.gridx = 0;
            constraints.gridy = 0;
            constraints.gridwidth = 2;
            constraints.anchor = GridBagConstraints.NORTHWEST;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.weightx = 0;
            constraints.weighty = 0;
            constraints.insets = new Insets(0, 0, UIConstants.VERTICAL_GAP, 0);

            tfName = new XTextField(getOutputName());
            JPanel pnlName = LabelComponentPanel.createVertical("dlg.zip.lbl.name", tfName);
            pnlCenter.add(pnlName, constraints);

            Path currentDirectory = getDirectory(xpnl);
            Path otherDirectory = getDirectory(XPanels.getOtherPanel(xpnl));
            if (!currentDirectory.equals(otherDirectory)) {
                rbCurrentPanel = new XRadioButton("dlg.zip.lbl.this.panel", true);
                rbOtherPanel = new XRadioButton("dlg.zip.lbl.other.panel");
                ButtonGroup bg = new ButtonGroup();
                bg.add(rbCurrentPanel);
                bg.add(rbOtherPanel);

                constraints.gridy++;
                constraints.gridwidth = 1;
                pnlCenter.add(rbCurrentPanel, constraints);

                constraints.gridx++;
                constraints.insets = new Insets(0, UIConstants.HORIZONTAL_GAP, UIConstants.VERTICAL_GAP, UIConstants.HORIZONTAL_GAP);
                pnlCenter.add(rbOtherPanel, constraints);
            }

            JPanel pnlPaths = LabelComponentPanel.createVertical("dlg.zip.lbl.paths", new JScrollPane(getPathsList(paths)));

            constraints.gridx = 0;
            constraints.gridy++;
            constraints.gridwidth = 2;
            constraints.fill = GridBagConstraints.BOTH;
            constraints.weightx = 1;
            constraints.weighty = 1;
            constraints.insets = new Insets(0, 0, 0, 0);
            pnlCenter.add(pnlPaths, constraints);

            this.setCenterComponent(pnlCenter);
            this.setVisibleRelativeToParent(1.0, null);
        }

        private String getOutputName() {
            assert(paths != null);
            Path base = getDirectory(xpnl);
            PathProcessor processor = xpnl.getTable().getContext().getPathProcessor();
            String name;
            if (paths.length > 1) {
                name = PathUtils.getName(base);
            } else {
                name = PathUtils.getName(paths[0]);
                name = FileExtension.getFileExtension(name).get(FileExtension.NAME);
            }
            Path path;
            String uniqueName = name;
            int i = 1;
            while (i < 100) {
                path = base.resolve(uniqueName + "." + PathUtils.getZipExtension());
                if (!processor.fileExists(path)) {
                    break;
                } else {
                    uniqueName = name + i++;
                }
            }
            return uniqueName + "." + PathUtils.getZipExtension();
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
            XPanel pnlOutput = (rbCurrentPanel == null || rbCurrentPanel.isSelected())?
                    xpnl : XPanels.getOtherPanel(xpnl);
            Path base = getDirectory(pnlOutput);

            Path output = base.resolve(tfName.getText());
            if (pnlOutput.getTable().getContext().getPathProcessor().fileExists(output)) {
                Messages.showMessage(ZipDialog.this, "err.path.already.exists", output);
                return false;
            }

            try {
                ZipWorker.create(output).packAll(paths);
            } catch (IOException e) {
                Errors.showError(ZipDialog.this, e);
            }
            return true;
        }
    }
}

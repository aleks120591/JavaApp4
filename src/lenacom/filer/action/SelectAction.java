package lenacom.filer.action;

import lenacom.filer.component.*;
import lenacom.filer.config.*;
import lenacom.filer.message.Errors;
import lenacom.filer.panel.PathRow;
import lenacom.filer.panel.XTable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.List;

class SelectAction extends XAction {

    public SelectAction(String key, boolean withEllipsis) {
        super(key, withEllipsis);
        this.setIcon(XIcon.SELECT.getIcon());
    }

    @Override
    public void act() {
        new SelectDialog(getTable());
    }

    private static class SelectDialog extends OkCancelDialog {
        private final static String MASK_PANEL = "mask.panel";
        private final static String EXTENSIONS_PANEL = "extensions.panel";

        private JPanel pnlCards;
        private CardLayout cardLayout;
        private ExtensionsPanel pnlExtensions;
        private XToggleButton btnExtensions;
        private XTextField tfMask;

        private String SELECT_TOOLTIP;

        private static final String DEFAULT_MASK = "*";
        protected XTable xtbl;

        SelectDialog(XTable xtbl) {
            super(xtbl.getWrapper(), "dlg.select.title");
            this.xtbl = xtbl;

            tfMask = new XTextField(DEFAULT_MASK);
            tfMask.getDocument().addDocumentListener(new EditableDocumentListener() {
                @Override
                protected void onEveryUpdate() {
                    setOkEnabled();
                }
            });
            LabelComponent labelComponent = new LabelComponent(new ResourceKey("dlg.select.lbl.select"), tfMask, getSelectTooltip());
            JPanel pnlMask = new JPanel(new GridBagLayout());
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.gridx = 0;
            constraints.gridy = 0;
            constraints.anchor = GridBagConstraints.NORTHWEST;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.weightx = 1.0;
            constraints.insets = new Insets(0, 0, 0, 0);
            pnlMask.add(labelComponent.getLabel(), constraints);
            constraints.gridy++;
            constraints.insets = new Insets(UIConstants.VERTICAL_GAP, 0, 0, 0);
            pnlMask.add(tfMask, constraints);

            cardLayout = new CardLayout();
            pnlCards = new JPanel(cardLayout);
            pnlCards.add(pnlMask, MASK_PANEL);
            cardLayout.show(pnlCards, MASK_PANEL);

            String[] extensions = getExtensions(xtbl);
            if (extensions.length > 0) {
                pnlExtensions = new ExtensionsPanel(extensions);
                pnlCards.add(pnlExtensions, EXTENSIONS_PANEL);

                btnExtensions = new XToggleButton("dlg.select.btn.extensions");
                ActionListener listener = new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (btnExtensions.isSelected()) {
                            cardLayout.show(pnlCards, EXTENSIONS_PANEL);
                            Component c = pnlExtensions.getComponent(0);
                            if (c != null) c.requestFocus();
                        } else {
                            updateMask();
                            cardLayout.show(pnlCards, MASK_PANEL);
                            tfMask.requestFocus();
                        }
                        setOkEnabled();
                    }
                };
                btnExtensions.addActionListener(listener);
                registerButton(btnExtensions, KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK);
                btnExtensions.setToolTipText("Ctrl-E");
                this.addLeftButtons(btnExtensions);
            }

            this.setCenterComponent(pnlCards);
            setVisibleRelativeToParent(1.0, null);
        }

        private String getSelectTooltip() {
            if (SELECT_TOOLTIP == null) {
                try {
                    StringBuilder sb = new StringBuilder();
                    sb.append("<html>");
                    sb.append(Resources.getGlobTooltip());
                    sb.append("</html>");
                    SELECT_TOOLTIP = sb.toString();
                } catch (Exception e) {
                    Errors.showError(this, e);
                    SELECT_TOOLTIP = "";
                }
            }
            return SELECT_TOOLTIP;
        }

        private void setOkEnabled() {
            if (pnlExtensions.isVisible()) {
                setOkEnabled(pnlExtensions.getSelectedExtensions().size() > 0);
            } else {
                setOkEnabled(!tfMask.getText().isEmpty());
            }
        }

        private void updateMask() {
            Set<String> selectedExtensions = pnlExtensions.getSelectedExtensions();
            if (selectedExtensions.size() > 0) {
                StringBuilder sb = new StringBuilder();
                for (String extension : selectedExtensions) {
                    if (sb.length() > 0) sb.append(",");
                    sb.append("*.").append(extension);
                }
                if (selectedExtensions.size() > 1) {
                    sb.insert(0, "{");
                    sb.append("}");
                }
                tfMask.setText(sb.toString());
            }
        }

        private String[] getExtensions(XTable xtbl) {
            SortedSet<String> set = new TreeSet<>();
            for (PathRow row: xtbl.getAllRows()) {
                String extension = row.getExtension().toLowerCase();
                if (!extension.isEmpty()) set.add(extension);
            }
            return set.toArray(new String[set.size()]);
        }

        @Override
        protected boolean onOk() {
            if (pnlExtensions.isVisible()) updateMask();
            String mask = tfMask.getText();
            mask = mask.replaceAll("\\*{2,}", "*");
            List<Path> paths = new ArrayList<>();
            PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:**\\" + File.separatorChar + mask);
            PathRow[] rows = xtbl.getAllRows();
            PathRow parentRow = xtbl.getParentRow();
            for (PathRow row : rows) {
                if (row == parentRow) continue;
                Path path = row.getPath();
                if (path != null && matcher.matches(path)) {
                    paths.add(path);
                }
            }
            xtbl.setSelectedPaths(paths.toArray(new Path[paths.size()]));
            return true;
        }

        private class ExtensionsPanel extends JPanel implements ActionListener {
            private Set<String> selectedExtensions = new HashSet<>();
            private final static int EXTENSIONS_IN_ROWS = 6;

            public ExtensionsPanel(String[] extensions) {
                this.setLayout(new GridBagLayout());
                int x = 0, y = 0;
                GridBagConstraints constraints = new GridBagConstraints();
                for(String extension: extensions) {
                    constraints.gridx = x++;
                    constraints.gridy = y;
                    constraints.anchor = GridBagConstraints.NORTHWEST;
                    constraints.fill = GridBagConstraints.NONE;
                    constraints.weightx = x == EXTENSIONS_IN_ROWS? 1.0 : 0;
                    constraints.insets = new Insets(0, x == 0? 0 : UIConstants.HORIZONTAL_GAP, 0, 0);
                    if (x == EXTENSIONS_IN_ROWS) {
                        x = 0;
                        y++;
                    }

                    JCheckBox chb = new JCheckBox(extension);
                    chb.addActionListener(this);
                    add(chb, constraints);
                }
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                JCheckBox chb = (JCheckBox) e.getSource();
                String extension = (chb).getText();
                if (chb.isSelected()) {
                    selectedExtensions.add(extension);
                } else {
                    selectedExtensions.remove(extension);
                }
                setOkEnabled();
            }

            public Set<String> getSelectedExtensions() {
                return selectedExtensions;
            }
        }
    }
}
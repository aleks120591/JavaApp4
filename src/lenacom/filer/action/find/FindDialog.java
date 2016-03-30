package lenacom.filer.action.find;

import lenacom.filer.config.*;
import lenacom.filer.message.Errors;
import lenacom.filer.path.PathUtils;
import lenacom.filer.root.ModelessDialogs;
import lenacom.filer.component.*;
import lenacom.filer.message.Messages;
import lenacom.filer.panel.XTable;
import lenacom.filer.panel.XTableContext;
import lenacom.filer.util.BackForwardStack;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class FindDialog extends AbstractDialog implements ActionListener, Refreshable {
    private static final String FOUND_TEMPLATE = Resources.getMessage("dlg.find.found.template");
    private static final int CONTAINS_MIN_LENGTH = 2;
    private static final int CONTAINS_MAX_LENGTH = 128;
    private XTableContext context;
    private OtherTableHandler otherTableHandler;
    private HistoryTextField tfName, tfContains;
    private JCheckBox chbMatchCase, chbAllExtracts, chbFindInFound;
    private JButton btnClear, btnBack, btnForward, btnAdvanced, btnClose;
    private JToggleButton btnFind, btnMaximize;
    private JLabel lblResult, lblStatus, lblFound;
    private ResultTable tblResult;
    private AbstractFindWorker worker;
    private String NAME_TOOLTIP;

    private BackForwardStack<Result> resultStack = new BackForwardStack<Result>() {
        @Override
        public void activate(Result result) {
            tblResult.clear();

            FindParameters params = result.getFindParameters();
            //set extracts visible before setting paths
            tblResult.setExtractsVisible(params.getContains() != null);
            lblStatus.setText(params.toString());

            addPaths(result.getPaths());
            tblResult.getTable().requestFocus();

            //we don't keep empty results in history
            chbFindInFound.setEnabled(true);
            tblResult.getTable().getSelectionModel().setSelectionInterval(0, 0);
        }
    };

    public FindDialog(Component parent, XTableContext context, XTable tblOther) {
        super(parent);
        //we fix the context at the beginning because the dialog it modeless and the context can change
        this.context = context;
        this.otherTableHandler = new OtherTableHandler(this, tblOther);

        this.setModalityType(ModalityType.MODELESS);
        this.setTitle(new ResourceKey("dlg.find.title", context.getDirectory().toString()));

        initComponents();
        JPanel pnlMain = getMainPanel();
        pnlMain.setBorder(XEmptyBorder.create());
        this.setLayout(new GridLayout(1, 1, 0, 0));
        this.add(pnlMain);

        ActionListener listener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tblResult.showContextMenu();
            }
        };
        //the same key as for the main context menu
        registerActionListener(listener, KeyEvent.VK_ENTER, InputEvent.SHIFT_DOWN_MASK);

        boolean maximize = Configuration.getBoolean(Configuration.MAXIMIZE_FIND, false);
        if (maximize) {
            maximize();
            setVisible(true);
            tblResult.adjustColumnWidths();
        } else {
            setVisibleRelativeToParent(1.0, 1.0);
        }
        btnMaximize.setSelected(maximize);

        ModelessDialogs.add(this);
    }

    private void initComponents() {
        btnFind = new XToggleButton("dlg.find.btn.search");
        btnFind.addActionListener(this);
        btnFind.setToolTipText("Ctrl-F");
        btnFind.setIcon(XIcon.FIND.getIcon());
        registerButton(btnFind, KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK);

        btnClear = XButton.create("dlg.find.btn.clear");
        btnClear.addActionListener(this);
        btnClear.setIcon(XIcon.BROOM.getIcon());
        btnClear.setToolTipText("Ctrl-L");
        registerButton(btnClear, KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK);

        btnClose = createCancelButton("btn.close");
        btnClose.setIcon(XIcon.CLOSE.getIcon());
        btnClose.setToolTipText("Ctrl-Q");
        registerButton(btnClose, KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK);

        btnBack = XButton.create("dlg.find.btn.back");
        btnBack.addActionListener(this);
        btnBack.setIcon(XIcon.BACK.getIcon());
        btnBack.setToolTipText("Ctrl-Shift-Left");
        registerButton(btnBack, KeyEvent.VK_LEFT, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_MASK);

        btnForward = XButton.create("dlg.find.btn.forward");
        btnForward.addActionListener(this);
        btnForward.setIcon(XIcon.FORWARD.getIcon());
        btnForward.setToolTipText("Ctrl-Shift-Right");
        registerButton(btnForward, KeyEvent.VK_RIGHT, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_MASK);

        enableBackForwardButtons();

        btnMaximize = new MaximizeButton("dlg.find.btn.maximize", "dlg.find.btn.minimize");
        btnMaximize.addActionListener(this);
        btnMaximize.setToolTipText("Ctrl-M");
        registerButton(btnMaximize, KeyEvent.VK_M, InputEvent.CTRL_DOWN_MASK);

        chbFindInFound = new XCheckBox("dlg.find.lbl.find.in.found", false);
        chbFindInFound.setEnabled(false);

        tfName = new HistoryTextField(History.FIND_PATH_NAME_HISTORY) {
            @Override
            protected void onEveryUpdate() {
                enableSearchClearButtons();
            }
        };

        if (!context.isZip()) {
            tfContains = new ExtendedHistoryTextField(History.FIND_TEXT_HISTORY) {
                @Override
                protected void onEveryUpdate() {
                    enableSearchClearButtons();
                    enableContainsOptions();
                }
            };

            chbMatchCase = new XCheckBox("dlg.find.lbl.match.case");
            chbAllExtracts = new XCheckBox("dlg.find.lbl.all.extracts");
            btnAdvanced = XButton.createWithEllipsis(new ResourceKey("dlg.find.btn.advanced"));
            btnAdvanced.addActionListener(this);
        }

        lblStatus = new JLabel() {
            @Override
            public void setText(String text) {
                super.setText(text);
                if (!this.isVisible()) this.setVisible(true);
            }
        };
        lblStatus.setVisible(false);

        lblFound = new JLabel();
        lblFound.setVisible(false);

        tblResult = new ResultTable(context, otherTableHandler);

        lblResult = XLabel.createWithTooltipAndColon(new ResourceKey("dlg.find.lbl.result"), new ResourceKey("dlg.find.lbl.result.tooltip"));
        lblResult.setLabelFor(tblResult.getTable());
    }

    private String getNameTooltip() {
        if (NAME_TOOLTIP == null) {
            try {
                StringBuilder sb = new StringBuilder();
                sb.append("<html>");
                sb.append(Resources.getMessage("dlg.find.lbl.name.tooltip")).append("<br><br>");
                sb.append(Resources.getGlobTooltip());
                sb.append("</html>");
                NAME_TOOLTIP = sb.toString();
            } catch (Exception e) {
                Errors.showError(this, e);
                NAME_TOOLTIP = Resources.getMessage("dlg.find.lbl.name.tooltip");
            }
        }
        return NAME_TOOLTIP;
    }

    private JPanel getMainPanel() {
        boolean isZip = context.isZip();

        LabelComponentTablePanel pnlConditions = new LabelComponentTablePanel();
        pnlConditions.addRow(new ResourceKey("dlg.find.lbl.name"), tfName, getNameTooltip());
        if (!isZip) pnlConditions.addRow(new ResourceKey("dlg.find.lbl.contains"),
                tfContains, new ResourceKey("tooltip.special.chars.history"));

        List<JComponent> components = new ArrayList<>();
        components.add(chbFindInFound);
        if (!isZip) {
            components.add(chbAllExtracts);
            components.add(chbMatchCase);
            components.add(btnAdvanced);
        }

        JComponent options = new HorizontalPanel(components.toArray(new JComponent[components.size()]));
        JPanel pnlFindClear = new VerticalPanel(btnFind, btnClear);

        JPanel pnlLabels = new JPanel(new XBorderLayout());
        pnlLabels.add(lblResult, BorderLayout.WEST);
        pnlLabels.add(lblFound, BorderLayout.EAST);

        JPanel pnlButtons = new JPanel(new XBorderLayout());
        pnlButtons.add(new HorizontalPanel(btnMaximize, btnBack, btnForward), BorderLayout.WEST);
        pnlButtons.add(btnClose, BorderLayout.EAST);

        JPanel pnlMain = new JPanel(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 1;
        constraints.weighty = 0;
        constraints.gridwidth = 1;
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(0, 0, 0, UIConstants.HORIZONTAL_GAP);
        pnlMain.add(pnlConditions, constraints);

        constraints.gridx = 1;
        constraints.weightx = 0;
        if (isZip) constraints.gridheight = 2;
        constraints.insets = new Insets(0, 0, 0, 0);
        pnlMain.add(pnlFindClear, constraints);

        constraints.gridx = 0;
        constraints.gridy++;
        constraints.gridheight = 1;
        if (!isZip) constraints.gridwidth = 2;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(UIConstants.VERTICAL_GAP, 0, 0, UIConstants.HORIZONTAL_GAP);
        pnlMain.add(options, constraints);

        constraints.gridy++;
        constraints.gridwidth = 2;
        constraints.insets = new Insets(UIConstants.VERTICAL_GAP, 0, 0, 0);
        pnlMain.add(pnlLabels, constraints);

        constraints.gridx = 0;
        constraints.gridy++;
        constraints.weighty = 1;
        constraints.fill = GridBagConstraints.BOTH;
        pnlMain.add(tblResult, constraints);

        constraints.gridx = 0;
        constraints.gridy++;
        constraints.weighty = 0;
        constraints.fill = GridBagConstraints.NONE;
        pnlMain.add(lblStatus, constraints);

        constraints.gridy++;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        pnlMain.add(pnlButtons, constraints);

        return pnlMain;
    }

    private void enableContainsOptions() {
        boolean enable = !tfContains.getText().isEmpty();
        chbMatchCase.setEnabled(enable);
        chbAllExtracts.setEnabled(enable);
        btnAdvanced.setEnabled(enable);
    }

    private void enableSearchClearButtons() {
        boolean empty = tfName.getText().isEmpty();
        if (tfContains != null && !tfContains.getText().isEmpty()) empty = false;
        btnFind.setEnabled(!empty || btnFind.isSelected());
        btnClear.setEnabled(!empty);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (btnFind == source) {
            if (btnFind.isSelected()) {
                find();
            } else {
                if (worker != null) worker.cancel(true);
                done();
            }
        } else if (btnClear == source) {
            tfName.setText("");
            if (tfContains != null) tfContains.setText("");
        } else if (btnBack == source) {
            resultStack.back();
            enableBackForwardButtons();
        } else if (btnForward == source) {
            resultStack.forward();
            enableBackForwardButtons();
        } else if (btnAdvanced == source) {
            new AdvancedDialog(this);
        } else if (btnMaximize == source) {
            toggleMaximize();
        }
    }

    private void toggleMaximize() {
        if (btnMaximize.isSelected()) {
            maximize();
        } else {
            setVisibleRelativeToParent(1.0, 1.0);
        }
        btnMaximize.requestFocus();
        tblResult.adjustColumnWidths();
    }

    private boolean validateParameters(FindParameters params) {
        String contains = params.getContains();
        if (contains != null) {
            if (!contains.isEmpty() && (contains.length() < CONTAINS_MIN_LENGTH || contains.length() > CONTAINS_MAX_LENGTH)) {
                Messages.showMessage(FindDialog.this, "dlg.find.err.contains.size", CONTAINS_MIN_LENGTH, CONTAINS_MAX_LENGTH);
                tfContains.requestFocus();
                return false;
            }
        }
        //we need this checking because disabling of buttons may not work
        return params.getName() != null || contains != null;
    }

    private void find() {
        Path base = context.getDirectory();

        FindParameters params = new FindParameters();
        String name = tfName.getText().trim();
        name = name.replaceAll("\\*{2,}", "*");
        params.setName(name);
        if (!context.isZip()) {
            params.setContains(tfContains.getText());
        }

        if (!validateParameters(params)) {
            btnFind.setSelected(false);
            return;
        }

        btnBack.setEnabled(false);
        btnForward.setEnabled(false);
        lblStatus.setText("");

        tfName.addHistoryItem(params.getName());
        if (params.getContains() != null) {
            tfContains.addHistoryItem(params.getContains());
            params.setCaseSensitive(chbMatchCase.isSelected());
            params.setAllExtracts(chbAllExtracts.isSelected());
        }
        tblResult.setColumnFoundExtractsVisible(params.getContains() != null);

        if (chbFindInFound.isSelected()) {
            worker = new FindInFoundWorker(this, tblResult.getPathsAsCopy(), base, params);
        } else {
            if (context.isZip()){
                //find within zip
                worker = new FirstFindWorker(this, base, params, context.getZipWorker());
            } else {
                //find native files
                worker = new FirstFindWorker(this, base, params);
            }
        }
        //clear list after we got path for find in found
        tblResult.clear();
        tblResult.setFindParameters(params);
        tblResult.setExtractsVisible(params.getContains() != null);
        tblResult.getTable().requestFocus();
        lblFound.setText(MessageFormat.format(FOUND_TEMPLATE, 0));

        worker.execute();
    }

    void done() {
        lblStatus.setVisible(false);
        List<FoundPath> paths = tblResult.getPathsAsCopy();
        Result result = new Result(tblResult.getFindParameters(), paths);
        if (paths.size() > 0) {
            tblResult.getTable().getSelectionModel().setSelectionInterval(0, 0);
        } else {
            chbFindInFound.setSelected(false);
        }
        resultStack.addItem(result);
        enableBackForwardButtons();
        chbFindInFound.setEnabled(paths.size() > 0);
        btnFind.setSelected(false);
    }

    @Override
    protected boolean onCancel() {
        if (worker != null) {
            worker.cancel(true);
        }
        otherTableHandler.restoreOriginalDirectory();
        Configuration.setBoolean(Configuration.MAXIMIZE_FIND, btnMaximize.isSelected());
        ModelessDialogs.remove(this);
        return true;
    }

    void currentPath(Path path) {
        lblStatus.setText(path.toString());
    }

    void found(List<FoundPath> paths) {
        addPaths(paths);
    }

    private void addPaths(List<FoundPath> paths) {
        tblResult.addPaths(paths);
        if (!lblFound.isVisible()) lblFound.setVisible(true);
        lblFound.setText(MessageFormat.format(FOUND_TEMPLATE, tblResult.countPaths()));
    }

    private void enableBackForwardButtons() {
        btnBack.setEnabled(resultStack.hasBackItem());
        btnForward.setEnabled(resultStack.hasForwardItem());
    }

    public void refresh() {
        tblResult.refresh();
        tfName.refresh();
        if (tfContains != null) tfContains.refresh();
    }

}

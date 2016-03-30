package lenacom.filer.action.find;

import lenacom.filer.component.*;
import lenacom.filer.config.Charsets;
import lenacom.filer.config.Colors;
import lenacom.filer.config.Configuration;
import lenacom.filer.config.Constants;
import lenacom.filer.message.Messages;
import lenacom.filer.path.FormattedPathSize;
import lenacom.filer.path.PathSizeUnit;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class AdvancedDialog extends OkCancelDialog implements ActionListener {
    private DefaultListModel<Charset> charsetsListModel;
    private JList<Charset> charsetsList;
    private JButton btnAddCharset, btnDeleteCharset;
    private JCheckBox chbLimitedSize;
    private XTextField tfSize;
    private JComboBox<PathSizeUnit> cbSizeUnit;

    AdvancedDialog(JDialog parent) {
        super(parent, "dlg.find.advanced.title");

        btnAddCharset = XButton.create("dlg.find.advanced.btn.add");
        btnAddCharset.addActionListener(this);
        btnDeleteCharset = XButton.create("dlg.find.advanced.btn.delete");
        btnDeleteCharset.addActionListener(this);
        btnDeleteCharset.setEnabled(false);

        charsetsListModel = new DefaultListModel<>();
        for (Charset charset: Charsets.getCharsets()) charsetsListModel.addElement(charset);
        charsetsList = new JList<>(charsetsListModel);
        charsetsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        charsetsList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                boolean enabled = charsetsList.getSelectedIndex() >= 0;
                btnDeleteCharset.setEnabled(enabled);
            }
        });
        charsetsList.setCellRenderer(new XListCellRenderer<>());
        charsetsList.setBackground(Colors.getBackground());

        JPanel pnlCharsets = new JPanel(new XBorderLayout());
        pnlCharsets.add(new JScrollPane(charsetsList), BorderLayout.CENTER);
        pnlCharsets.add(new VerticalPanel(btnAddCharset, btnDeleteCharset), BorderLayout.EAST);
        LabelComponentPanel pnlLabelCharsets = LabelComponentPanel.createVertical("dlg.find.advanced.lbl.charsets", pnlCharsets, "dlg.find.advanced.lbl.charsets.tooltip");

        boolean limitedSize = Configuration.getBoolean(Configuration.FIND_LIMITED_SIZE, false);
        chbLimitedSize = new XCheckBox("dlg.find.advanced.lbl.size.less", limitedSize);
        chbLimitedSize.addActionListener(this);

        Long size = Configuration.getLong(Configuration.TEXT_FILE_MAX_SIZE);
        if (size == null) size = Constants.DEFAULT_TEXT_FILE_MAX_SIZE;
        FormattedPathSize formattedTextFileMaxSize = new FormattedPathSize(size);
        String value = Long.toString(Math.round(formattedTextFileMaxSize.getValue()));

        tfSize = new NumberTextField(value, 5);
        tfSize.setEnabled(limitedSize);

        cbSizeUnit = new JComboBox<>(PathSizeUnit.values());
        cbSizeUnit.setEnabled(limitedSize);
        cbSizeUnit.setSelectedItem(formattedTextFileMaxSize.getSizeUnit());

        JPanel pnlOptions = new HorizontalPanel(chbLimitedSize,
                LabelComponentPanel.createHorizontal("dlg.find.advanced.lbl.than", tfSize), cbSizeUnit);

        JPanel pnlCenter = new JPanel(new XBorderLayout());
        pnlCenter.add(pnlLabelCharsets, BorderLayout.CENTER);
        pnlCenter.add(pnlOptions, BorderLayout.SOUTH);
        this.setCenterComponent(pnlCenter);

        super.setVisibleRelativeToParent();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (btnDeleteCharset == source) {
            int index = charsetsList.getSelectedIndex();
            charsetsListModel.removeElementAt(charsetsList.getSelectedIndex());
            if (index >= charsetsListModel.getSize()) index--;
            if (index >= 0) charsetsList.setSelectedIndex(index);
        } else if (btnAddCharset == source) {
            new AddCharsetDialog(AdvancedDialog.this);
        } else if (chbLimitedSize == source) {
            boolean enable = chbLimitedSize.isSelected();
            tfSize.setEnabled(enable);
            cbSizeUnit.setEnabled(enable);
        }
    }

    @Override
    protected boolean onOk() {
        if (charsetsList.getModel().getSize() == 0) {
            Messages.showMessage(AdvancedDialog.this, "dlg.find.advanced.err.charsets.empty");
            return false;
        }

        List<Charset> charsets = new ArrayList<>(charsetsListModel.size());
        for (int i = 0, n = charsetsListModel.size(); i < n; i++) {
            charsets.add(charsetsListModel.elementAt(i));
        }
        Charsets.setCharsets(charsets);

        Configuration.setBoolean(Configuration.FIND_LIMITED_SIZE, chbLimitedSize.isSelected());

        try {
            long maxSize = Integer.parseInt(tfSize.getText()) * ((PathSizeUnit) cbSizeUnit.getSelectedItem()).getBytes();
            Configuration.setLong(Configuration.TEXT_FILE_MAX_SIZE, maxSize);
        } catch (NumberFormatException e) {
            if (chbLimitedSize.isSelected()) {
                Messages.showMessage(AdvancedDialog.this, "dlg.find.advanced.err.wrong.max.size");
                return false;
            }
        }

        return true;
    }

    private class AddCharsetDialog extends ListDialog<Charset> {

        private AddCharsetDialog(JDialog parent) {
            super(parent, "dlg.add.charset.title");
            Collection<Charset> allCharsets = Charset.availableCharsets().values();
            List<Charset> availableCharsets = new ArrayList<>(allCharsets.size());
            for (Charset charset: allCharsets) {
                if (!charsetsListModel.contains(charset)) {
                    availableCharsets.add(charset);
                }
            }
            setList(availableCharsets.toArray(new Charset[availableCharsets.size()]));
            this.setVisibleRelativeToParent(0.5, 1.0);
        }

        @Override
        public void valueChanged(ListSelectionEvent e) {
            //do nothing
        }

        @Override
        protected boolean onOk() {
            charsetsListModel.addElement(getSelectedValue());
            return true;
        }
    }
}

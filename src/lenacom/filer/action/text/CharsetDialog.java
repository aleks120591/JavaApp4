package lenacom.filer.action.text;

import lenacom.filer.component.LabelComponentPanel;
import lenacom.filer.component.OkCancelDialog;
import lenacom.filer.component.UIConstants;
import lenacom.filer.component.XListCellRenderer;
import lenacom.filer.config.Charsets;
import lenacom.filer.config.Colors;
import lenacom.filer.config.Fonts;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;

abstract class CharsetDialog extends OkCancelDialog {
    private Charset charset;
    private java.util.List<Charset> charsets;
    private JList<Charset> listAvailableCharsets;
    private JTextArea taSample;
    private final byte[] sample;

    CharsetDialog(JDialog owner, byte[] bytes) {
        super(owner, "dlg.file.charset.title");

        this.sample = getSample(bytes);

        taSample = new JTextArea(10, 50);
        taSample.setEditable(false);
        taSample.setLineWrap(true);
        taSample.setFont(Fonts.getFont());
        taSample.setBackground(Colors.getBackground());
        taSample.setSelectionColor(Colors.getForeground());
        taSample.setForeground(Colors.getForeground());
        taSample.setSelectedTextColor(Colors.getBackground());

        initCharsets();
        decodeSample(charset);

        JPanel pnlCenter = new JPanel(new GridLayout(1, 0, UIConstants.BORDER_GAP, 0));
        pnlCenter.add(LabelComponentPanel.createVertical("dlg.file.charset.lbl.charset", new JScrollPane(listAvailableCharsets)));
        pnlCenter.add(LabelComponentPanel.createVertical("dlg.file.charset.lbl.sample", new JScrollPane(taSample)));
        this.setCenterComponent(pnlCenter);

        listAvailableCharsets.ensureIndexIsVisible(listAvailableCharsets.getSelectedIndex());

        super.setVisibleRelativeToParent(1.0, 0.5);
    }

    private byte[] getSample(byte[] bytes) {
        final int MAX_LENGTH = 1024;
        byte[] sample;
        if (bytes.length < MAX_LENGTH) {
            sample = bytes;
        }
        else {
            sample = new byte[MAX_LENGTH];
            System.arraycopy(bytes, 0, sample, 0, sample.length);
        }
        return sample;
    }

    public Charset getSelectedCharset() {
        return charsets.get(listAvailableCharsets.getSelectedIndex());
    }

    private void initCharsets() {
        charset = Charsets.getDefaultCharset();
        charsets = new java.util.ArrayList<>(Charset.availableCharsets().values());
        listAvailableCharsets = new JList<>(charsets.toArray(new Charset[charsets.size()]));
        listAvailableCharsets.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listAvailableCharsets.setSelectedValue(charset, true);
        listAvailableCharsets.setCellRenderer(new XListCellRenderer<>());

        listAvailableCharsets.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                decodeSample(charsets.get(listAvailableCharsets.getSelectedIndex()));
            }
        });

        listAvailableCharsets.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    if (onOk()) dispose();
                }
            }
        });
    }
    
    private void decodeSample(Charset charset) {
        String text = "";
        try {
            text = charset.newDecoder().decode(ByteBuffer.wrap(sample)).toString();
        } catch (CharacterCodingException e) {
            //failed to decode
        }
        taSample.setText(text);
        taSample.setCaretPosition(0);
    }
}

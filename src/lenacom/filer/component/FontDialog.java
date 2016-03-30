package lenacom.filer.component;

import lenacom.filer.message.Messages;

import javax.swing.*;
import java.awt.*;

public abstract class FontDialog extends ApplyDialog {
    private JList<String> listFontFamilyNames;
    private XTextField tfFontSize;
    private Font font;

    public FontDialog(Component parent, Font font) {
        super(parent, "dlg.file.font.title");
        this.font = font;

        GraphicsEnvironment e = GraphicsEnvironment.getLocalGraphicsEnvironment();
        listFontFamilyNames = new JList<>(e.getAvailableFontFamilyNames());
        listFontFamilyNames.setVisibleRowCount(10);
        listFontFamilyNames.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listFontFamilyNames.setCellRenderer(new XListCellRenderer<String>());

        tfFontSize = new NumberTextField(String.valueOf(font.getSize()), 5);

        LabelComponentTablePanel pnlFontSettings = new LabelComponentTablePanel();
        pnlFontSettings.addRow("dlg.file.font.lbl.family", new JScrollPane(listFontFamilyNames));
        pnlFontSettings.addRow("dlg.file.font.lbl.size", tfFontSize);
        this.setCenterComponent(pnlFontSettings);

        //set value and scroll after the list is wrapped
        listFontFamilyNames.setSelectedValue(font.getFamily(), true);

        setApplyEnabled(true);
        setVisibleRelativeToParent();
    }

    @Override
    protected boolean apply() {
        String fontFamily = listFontFamilyNames.getSelectedValue();
        Integer fontSize = null;
        try {
            fontSize = Integer.parseInt(tfFontSize.getText());
        } catch (NumberFormatException e) {
            //do nothing
        }
        if (fontSize != null && fontSize > 0) {
            Font font = new Font(fontFamily, Font.PLAIN, fontSize);
            applyFont(font);
            return true;
        } else {
            Messages.showMessage(FontDialog.this, "dlg.file.font.err.invalid.size");
            return false;
        }
    }

    @Override
    protected boolean onCancel() {
        applyFont(font); //restore
        return true;
    }

    protected abstract void applyFont(Font font);
}

package lenacom.filer.component;

import lenacom.filer.config.Colors;
import lenacom.filer.config.Fonts;

import javax.swing.*;
import java.awt.*;

public class XListCellRenderer<T> extends RendererLabel implements ListCellRenderer<T> {

    public XListCellRenderer() {
        this.setFont(Fonts.getFont());
        this.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends T> list, T value, int index, boolean isSelected, boolean cellHasFocus) {
        this.setText(value.toString());
        this.setForeground(Colors.getListItemForeground(cellHasFocus));
        this.setBackground(Colors.getListItemBackground(cellHasFocus));
        return this;
    }
}

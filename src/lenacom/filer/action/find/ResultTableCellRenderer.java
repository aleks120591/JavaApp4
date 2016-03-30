package lenacom.filer.action.find;

import lenacom.filer.component.RendererLabel;
import lenacom.filer.config.Colors;
import lenacom.filer.config.Fonts;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

class ResultTableCellRenderer extends RendererLabel implements TableCellRenderer {
    private FontMetrics metrics;
    private ResultTable tblResult;
    private ResultTableColumnModel columnModel;
    private RendererLabel iconRenderer = new RendererLabel();

    ResultTableCellRenderer(ResultTable tblResult) {
        assert(tblResult.getTable() != null);
        this.tblResult = tblResult;
        initFont();
        this.columnModel = (ResultTableColumnModel) tblResult.getTable().getColumnModel();
    }

    FontMetrics getMetrics() {
        return metrics;
    }

    private void initFont() {
        Font font = Fonts.getFont();
        this.setFont(font);
        iconRenderer.setFont(font);
        metrics = getFontMetrics(font);
        tblResult.getTable().setRowHeight(metrics.getHeight());
    }

    void refresh() {
        initFont();
    }

    @Override
    public Component getTableCellRendererComponent(final JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int rowIndex, int colIndex) {
        ResultTableModelCell cell = (ResultTableModelCell) value;
        FoundPath foundPath = cell.getFoundPath();
        ResultColumn column = cell.getColumn();

        RendererLabel renderer;
        String text = "";
        int width = 0;

        if (column == ResultColumn.PATH) {
            renderer = iconRenderer;
            renderer.setIcon(foundPath.getIcon());
            text = foundPath.getParent();
            Icon icon = foundPath.getIcon();
            if (icon != null) width += icon.getIconWidth();
        } else {
            renderer = this;
            if (column == ResultColumn.NAME) {
                text = foundPath.getName();
            } else if (column == ResultColumn.EXTENSION && foundPath instanceof FoundFile) {
                text = ((FoundFile) foundPath).getExtension();
            } else if (column == ResultColumn.FOUND_EXTRACTS && foundPath instanceof FoundFile) {
                text = String.valueOf(((FoundFile) foundPath).getCountExtracts());
            }
        }

        renderer.setText(text);
        if (foundPath instanceof FoundFile) {
            FoundFile foundFile = (FoundFile) foundPath;
            renderer.setBackground(foundFile.getBackground());
        } else {
            renderer.setBackground(Colors.getBackground());
        }
        renderer.setForeground(isSelected? Colors.getSelectedForeground() : Colors.getForeground());

        width += metrics.stringWidth(text);
        int colWidth = columnModel.getColumn(column).getWidth();
        String tooltip = width > colWidth? foundPath.getTooltip() : null;
        renderer.setToolTipText(tooltip);

        return renderer;
    }

    @Override
    public void updateUI() {
        super.updateUI();
        if (iconRenderer != null) iconRenderer.updateUI();
    }
}

package lenacom.filer.panel;

import lenacom.filer.component.RendererLabel;
import lenacom.filer.config.Colors;
import lenacom.filer.config.Fonts;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.text.MessageFormat;

class XTableCellRenderer extends RendererLabel implements TableCellRenderer {
    private static final String SYMLINK_HTML = "<html><table cellpadding=0 cellspacing=0><tr><td>symlink:&nbsp;</td><td>{0}</td></tr><tr><td>target:&nbsp;</td><td>{1}</td></tr></table></html>";
    private FontMetrics metrics;
    private XTableImpl xTable;
    private JLabel nameRenderer;

    XTableCellRenderer(XTableImpl xtbl) {
        this.xTable = xtbl;
        nameRenderer = new RendererLabel();
        initFont();
    }

    FontMetrics getMetrics() {
        return metrics;
    }

    private void initFont() {
        Font font = Fonts.getFont();
        nameRenderer.setFont(font);
        this.setFont(font);
        metrics = getFontMetrics(font);
        xTable.getTable().setRowHeight(metrics.getHeight());
    }

    void refresh() {
        initFont();
    }

    @Override
    public Component getTableCellRendererComponent(final JTable table, Object cellObject,
        boolean isSelected, boolean hasFocus, int rowIndex, int colIndex) {
        XTableModelCell cell = (XTableModelCell) cellObject;
        final PathRow row = cell.getRow();
        XColumn column = cell.getColumn();

        JLabel renderer;
        String value = XValue.getValue(row, column);
        if (column == XColumn.NAME) {
            nameRenderer.setIcon(row.getIcon());
            String tooltip = null;
            if (row.isSymlink()) {
                tooltip = MessageFormat.format(SYMLINK_HTML, row.getPath(), row.getSymlinkTarget());
            } else {
                int width = metrics.stringWidth(value);
                Icon icon = row.getIcon();
                if (icon != null) width += icon.getIconWidth();
                int colWidth = xTable.getColumnModel().getColumn(XColumn.NAME).getWidth();
                tooltip = width > colWidth? value : null;
            }
            nameRenderer.setToolTipText(tooltip);
            renderer = nameRenderer;
        } else {
            renderer = this;
        }
        renderer.setText(value);

        Color foreground;
        if (isSelected) {
            boolean active = XPanels.getActiveTable() == xTable;
            foreground = Colors.getSelectedForeground(active);
        } else {
            foreground = column == XColumn.SIZE && row instanceof DirectoryRow && row.isSizeApproximate()?
                    Colors.getDisabledForeground() :
                    Colors.getForeground();
        }
        Color  background = row instanceof FileRow ? ((FileRow) row).getBackground() :
                    Colors.getBackground();
        renderer.setForeground(foreground);
        renderer.setBackground(background);

        if (hasFocus) {
            final int oldFocusedRow = xTable.getFocusedRowIndex();
            xTable.setFocusedRowIndex(rowIndex);
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < table.getColumnCount(); i++) {
                        table.repaint(table.getCellRect(oldFocusedRow, i, true));
                    }
                }
            });
        }

        return renderer;
    }

    @Override
    public void updateUI() {
        super.updateUI();
        if (nameRenderer != null) nameRenderer.updateUI();
        metrics = getFontMetrics(getFont());
    }
}

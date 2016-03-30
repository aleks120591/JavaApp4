package lenacom.filer.panel;

import lenacom.filer.config.Colors;
import lenacom.filer.config.Configuration;
import lenacom.filer.menu.ContextMenu;
import lenacom.filer.path.PathUtils;

import javax.swing.*;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.nio.file.Path;
import java.util.*;
import java.util.List;

class XTableImpl implements XTable {
    private final int id;
    private JScrollPane wrapper;
    private XTableModel model;
    private JTable table;
    private XTableSorter sorter;
    private XTableCellRenderer renderer;
    private XTableColumnAdjuster columnAdjuster;

    private int focusedRowIndex = 0;
    private XTableSelectionListener selectionListener;
    private PathStack pathStack;

    /* public methods */

    @Override
    public JComponent getWrapper() {
        return wrapper;
    }

    @Override
    public XTableContext getContext() {
        return model.getContextDirectory();
    }

    @Override
    public XColumn[] getColumns() {
        return ((XTableColumnModel) table.getColumnModel()).getXColumns();
    }

    @Override
    public void setContextDirectory(Path path) {
        if (path.equals(model.getContextDirectory().getDirectory())) return;
        model.setContextDirectory(path);
        pathStack.addItem(path);
    }

    @Override
    public PathRow[] getAllRows() {
        int n = getRowCount();
        PathRow[] rows = new PathRow[n];
        for (int i = 0; i < n; i++) {
            rows[i] = getRow(i);
        }
        return rows;
    }

    @Override
    public PathRow getFocusedRow() {
        return getRow(this.focusedRowIndex);
    }

    @Override
    public PathRow[] getSelectedRows() {
        List<PathRow> rows = new ArrayList<>();
        if (model.getRowCount() > 0) {
            for (int rowIndex : table.getSelectedRows()) {
                rows.add(getRow(rowIndex));
            }
        }
        return rows.toArray(new PathRow[rows.size()]);
    }

    @Override
    public Path[] getSelectedPaths() {
        PathRow[] selectedRows = getSelectedRows();
        Path[] paths = new Path[selectedRows.length];
        for (int i = 0; i < selectedRows.length; i++) {
            paths[i] = selectedRows[i].getPath();
        }
        return paths;
    }

    @Override
    public void refresh() {
        //refresh renderer before model
        XTableColumnModel columnModel = new XTableColumnModel();
        table.setColumnModel(columnModel);

        renderer.refresh();
        model.refresh();
        table.setBackground(Colors.getBackground());
    }

    @Override
    public PathStack getStack() {
        return pathStack;
    }

    @Override
    public void setSelectedPaths(Path... paths) {
        //we use first setSelectionInterval because of XTableSelectionListener
        boolean first = true;
        Set<Path> set = new HashSet<>(Arrays.asList(paths));
        ListSelectionModel selModel = table.getSelectionModel();
        //remove selection first
        for (int i = Math.max(selModel.getMinSelectionIndex(), 0),
                     last = selModel.getMaxSelectionIndex(); i <= last; i++) {
            if (!set.contains(getRow(i).getPath())) {
                selModel.removeSelectionInterval(i, i);
            }
        }
        //then add selection to have the focus on the last selected row
        for (int i = 0, size = table.getRowCount(); i < size; i++) {
            if (set.contains(getRow(i).getPath())) {
                if (first) {
                    selModel.setSelectionInterval(i, i);
                    first = false;
                } else {
                    selModel.addSelectionInterval(i, i);
                }
            }
        }
        Rectangle rect = table.getCellRect(table.getSelectedRow(), 0, true);
        table.scrollRectToVisible(rect);
    }

    @Override
    public void selectAll(){
        //we don't use table.selectAll() because of XTableSelectionListener
        int firstRowIndex = model.getParentRow() == null? 0 : 1;
        int lastRowIndex = getRowCount() - 1;
        table.getSelectionModel().setSelectionInterval(Math.min(firstRowIndex, lastRowIndex), lastRowIndex);
    }

    @Override
    public void setSortColumn(XColumn col) {
        sorter.sortByColumn(col);
    }

    @Override
    public XColumn getSortColumn() {
        return sorter.getSortColumn();
    }

    @Override
    public void addListener(XTableListener l) {
        model.addListener(l);
        selectionListener.addListener(l);
    }

    @Override
    public synchronized void removeListener(XTableListener l) {
        model.removeListener(l);
        selectionListener.removeListener(l);
    }

    @Override
    public ParentDirectoryRow getParentRow() {
        return model.getParentRow();
    }

    @Override
    public void goTo(Path path) {
        assert(path.isAbsolute());

        boolean dir = true;
        try {
            dir = getContext().getPathProcessor().isDirectory(path);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (dir) {
            setContextDirectory(path);
        } else {
            Path parent = path.getParent();
            if (parent != null) {
                setContextDirectory(parent);
                setSelectedPaths(path);
            }
        }
    }

    @Override
    public void showContextMenu() {
        JPopupMenu menu = ContextMenu.getMenu();
        if (menu != null) {
            Rectangle cell = table.getCellRect(getFocusedRowIndex(), 0, true);
            int contextMenuHeight = menu.getHeight();
            int x = cell.x;
            int y = cell.y + cell.height;
            if (y + contextMenuHeight > table.getY() + table.getHeight()) {
                y = cell.y - contextMenuHeight;
            }
            menu.show(table, x, y);
        }
    }

    /* package private methods */

    void showContextMenu(int x, int y) {
        JPopupMenu menu = ContextMenu.getMenu();
        if (menu != null) menu.show(table, x, y);
    }

    PathRow getRow(int rowIndex) {
        rowIndex = sorter.convertRowIndexToModel(rowIndex);
        XTableModelCell cell = (XTableModelCell) model.getValueAt(rowIndex, 0);
        return cell.getRow();
    }

    int getRowCount() {
        return table.getRowCount();
    }

    int getFocusedRowIndex() {
        return focusedRowIndex;
    }

    void setFocusedRowIndex(int focusedRowIndex) {
        this.focusedRowIndex = focusedRowIndex;
    }

    JTable getTable() {
        return table;
    }

    XTableModel getModel() {
        return model;
    }

    XTableColumnModel getColumnModel() {
        return (XTableColumnModel) table.getColumnModel();
    }

    XTableImpl(int id) {
        this.id = id;
        model = new XTableModel();
        XTableColumnModel columnModel = new XTableColumnModel();
        table = new InnerTable(model, columnModel);

        sorter = new XTableSorter(model);
        table.setRowSorter(sorter);

        table.addMouseListener(new XTableMouseListener(this));
        renderer = new XTableCellRenderer(this);
        table.setDefaultRenderer(XTableModelCell.class, renderer);

        selectionListener = new XTableSelectionListener(this);
        table.getSelectionModel().addListSelectionListener(selectionListener);

        table.setColumnSelectionAllowed(false);
        table.setRowSelectionAllowed(true);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setShowGrid(false);
        table.setBackground(Colors.getBackground());
        table.setFillsViewportHeight(true);

        table.setDragEnabled(true);
        table.setDropMode(DropMode.ON);
        table.setTransferHandler(new XTransferHandler(this));

        pathStack = new PathStack(model);

        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        columnAdjuster = new XTableColumnAdjuster(this);
        table.addHierarchyListener(new HierarchyListener() {
            public void hierarchyChanged(HierarchyEvent e) {
                if ((HierarchyEvent.SHOWING_CHANGED & e.getChangeFlags()) != 0
                        && table.isShowing()) {
                    columnAdjuster.adjust(); //adjust columns first time after showing up
                }
            }
        });

        addListener(new XTableAdapter() {
            @Override
            public void workingDirectoryChanged(XTableContext newContext) {
                //directory may be changed by a watcher
                table.changeSelection(0, 0, false, false);
                focusedRowIndex = 0;
                Path path = newContext.getDirectory();
                Configuration.setPath(Configuration.CURRENT_PATH_KEY + XTableImpl.this.id, path);

                columnAdjuster.adjust();
            }
        });

        wrapper = new JScrollPane(table,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        wrapper.getInsets().left = wrapper.getInsets().right = 0;

        Path path = Configuration.getPath(Configuration.CURRENT_PATH_KEY + id);
        if (path == null) path = PathUtils.getDefaultRoot();
        setContextDirectory(path);
    }

    private final class InnerTable extends JTable {
        private final Stroke dashedStroke = new BasicStroke(1F, BasicStroke.CAP_SQUARE,
                                                           BasicStroke.JOIN_MITER, 3F, new float[]{2F, 2F}, 0F);
        private InnerTable(TableModel dm, TableColumnModel cm) {
            super(dm, cm);
            this.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    repaint();
                }
            });
        }

        @Override
        public void paint(Graphics graphics) {
            super.paint(graphics);
            //paint focused row
            Graphics2D graphics2D = (Graphics2D) graphics;
            graphics.setColor(Color.GRAY);
            graphics2D.setStroke(dashedStroke);
            Rectangle firstCell = table.getCellRect(focusedRowIndex, 0, false);
            graphics2D.drawRect(firstCell.x, firstCell.y, table.getSize().width - 1, table.getRowHeight() - 1);
        }
    }
}

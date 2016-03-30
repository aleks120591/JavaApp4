package lenacom.filer.action.find;

import lenacom.filer.action.*;
import lenacom.filer.component.XSplitPane;
import lenacom.filer.config.Colors;
import lenacom.filer.panel.XTableContext;
import lenacom.filer.path.PathUtils;
import lenacom.filer.util.KeyActionsUtils;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.List;

class ResultTable extends JPanel {
    private JTable table;
    private ResultTableModel model;
    private ResultTableColumnModel columnModel;
    private JScrollPane wrapperFoundPaths, wrapperExtracts;
    private JEditorPane paneExtracts;
    private JSplitPane splitPane;
    private ResultTableCellRenderer renderer;
    private ResultContextMenu contextMenu;
    private XTableContext context;
    private FindParameters params;
    private OtherTableHandler otherTableHandler;
    private TableColumn tcFoundExtracts;

    ResultTable(XTableContext context, OtherTableHandler otherTableHandler) {
        this.context = context;
        this.otherTableHandler = otherTableHandler;

        model = new ResultTableModel();
        columnModel = new ResultTableColumnModel();

        table = new JTable(model, columnModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setColumnSelectionAllowed(false);
        table.setRowSelectionAllowed(true);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setShowGrid(false);
        table.setFillsViewportHeight(true);
        table.setRowSorter(new ResultTableSorter(model));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        table.addHierarchyListener(new HierarchyListener() {
            public void hierarchyChanged(HierarchyEvent e) {
                if ((HierarchyEvent.SHOWING_CHANGED & e.getChangeFlags()) != 0
                        && table.isShowing()) {
                    //adjust columns first time after showing up
                    adjustColumnWidths();

                    Dimension size = new Dimension(0, 3 * ResultTable.this.getHeight() / 4);
                    wrapperFoundPaths.setPreferredSize(size);
                }
            }
        });

        renderer = new ResultTableCellRenderer(this);
        table.setDefaultRenderer(ResultTableModelCell.class, renderer);

        ActionListener listener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                goTo();
            }
        };
        KeyActionsUtils.registerActionListener(table, listener, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));

        wrapperFoundPaths = new JScrollPane(table,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        wrapperFoundPaths.getInsets().left = wrapperFoundPaths.getInsets().right = 0;

        paneExtracts = new JEditorPane("text/html", "");
        paneExtracts.setEditable(false);
        paneExtracts.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        wrapperExtracts = new JScrollPane(paneExtracts,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        wrapperExtracts.setMinimumSize(new Dimension(0, 0));

        splitPane = new XSplitPane(JSplitPane.VERTICAL_SPLIT, null, null);

        this.setLayout(new GridLayout(1, 1, 0, 0));
        this.add(wrapperFoundPaths);

        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                FoundPath foundPath = getSelectedFoundPath();
                String extracts = "";
                if (foundPath instanceof FoundFile) {
                    FoundFile foundFile = ((FoundFile) foundPath);
                    extracts = foundFile.getExtractsHtml();
                }
                paneExtracts.setText(extracts);
                paneExtracts.setCaretPosition(0);
            }
        });

        table.addMouseListener(new ResultTableMouseListener(this));
        contextMenu = new ResultContextMenu(this);

        tcFoundExtracts = table.getColumnModel().getColumn(ResultColumn.FOUND_EXTRACTS.ordinal());
        setColumnFoundExtractsVisible(false);

        setColors();
    }

    void adjustColumnWidths() {
        int tableWidth = wrapperFoundPaths.getViewport().getWidth(); //insets are 0
        EnumMap<ResultColumn, Integer> widths = new EnumMap<>(ResultColumn.class);
        int extensionWidth = renderer.getMetrics().stringWidth("abcde");
        widths.put(ResultColumn.EXTENSION, extensionWidth);
        //mind the found extracts column only if it's visible
        int foundExtractsWidth = columnModel.getColumnCount() == ResultColumn.values().length? renderer.getMetrics().stringWidth("abcde") : 0;
        widths.put(ResultColumn.FOUND_EXTRACTS, foundExtractsWidth);
        int nameWidth = (tableWidth - extensionWidth - foundExtractsWidth) * 3 / 10;
        widths.put(ResultColumn.NAME, nameWidth);
        int pathWidth = tableWidth - extensionWidth - foundExtractsWidth - nameWidth;
        widths.put(ResultColumn.PATH, pathWidth);

        for (ResultColumn column: ResultColumn.values()) {
            TableColumn tableColumn = columnModel.getColumn(column);
            Integer width = widths.get(column);
            table.getTableHeader().setResizingColumn(tableColumn);
            tableColumn.setWidth(width);
        }
    }

    private FoundPath getSelectedFoundPath() {
        int index = table.getSelectedRow();
        if (index >= 0 && index < table.getRowCount()) {
            index = table.getRowSorter().convertRowIndexToModel(index);
            return model.getPathAt(index);
        } else {
            return null;
        }
    }

    private Path getSelectedPath() {
        FoundPath foundPath = getSelectedFoundPath();
        if (foundPath != null) {
            Path path = foundPath.getPath();
            return context.getDirectory().resolve(path);
        } else {
            return null;
        }
    }

    void clear() {
        model.clear();
    }

    void addPaths(List<FoundPath> paths) {
        model.addAll(paths);
    }

    List<FoundPath> getPathsAsCopy() {
        return model.getPathsCopy();
    }

    JTable getTable() {
        return table;
    }

    FindParameters getFindParameters() {
        return params;
    }

    void setFindParameters(FindParameters params) {
        this.params = params;
    }

    void setExtractsVisible(boolean visible) {
        Component c = this.getComponent(0);
        if (c == splitPane && visible || c == wrapperFoundPaths && !visible) return;

        this.removeAll();
        if (visible) {
            splitPane.setLeftComponent(wrapperFoundPaths);
            splitPane.setRightComponent(wrapperExtracts);
            splitPane.resetToPreferredSizes();
            this.add(splitPane);
        } else {
            this.add(wrapperFoundPaths);
        }
        validate();
        adjustColumnWidths();
    }

    int countPaths() {
        return model.getRowCount();
    }

    void showContextMenu(int x, int y) {
        JPopupMenu menu = getMenu();
        if (menu != null) menu.show(table, x, y);
    }

    void showContextMenu() {
        JPopupMenu menu = getMenu();
        if (menu != null) {
            Rectangle cell = table.getCellRect(table.getSelectedRow(), 0, true);
            int contextMenuHeight = menu.getHeight();
            int x = cell.x;
            int y = cell.y + cell.height;
            if (y + contextMenuHeight > table.getY() + table.getHeight()) {
                y = cell.y - contextMenuHeight;
            }
            menu.show(table, x, y);
        }
    }

    private JPopupMenu getMenu() {
        Path path = getSelectedPath();
        if (path != null) {
            return PathUtils.isDirectory(path)?
                contextMenu.getDirectoryMenu() :
                contextMenu.getFileMenu();
        } else {
            return null;
        }
    }

    void goTo() {
        Path path = getSelectedPath();
        if (path != null) {
            otherTableHandler.getOtherTable().goTo(path);
            otherTableHandler.checkDirectoryChanged();
        }
    }

    void open() {
        Path path = getSelectedPath();
        if (path != null) {
            Actions.getOpenAction().open(context, path);
        }
    }

    void view() {
        Path path = getSelectedPath();
        if (path != null) {
            JComponent wrapper = otherTableHandler.getOtherTable().getWrapper();
            if (params.getContains() != null) {
                Actions.getViewAction().view(wrapper, context, path, params.getContains());
            } else {
                Actions.getViewAction().view(wrapper, context, path);
            }
        }
    }

    void edit() {
        Path path = getSelectedPath();
        if (path != null) {
            JComponent wrapper = otherTableHandler.getOtherTable().getWrapper();
            if (params.getContains() != null) {
                Actions.getEditAction().edit(wrapper, context, path, params.getContains());
            } else {
                Actions.getEditAction().edit(wrapper, context, path);
            }
        }
    }

    void copyPath() {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(new StringSelection(getSelectedPath().toString()), null);
    }

    private void setColors() {
        table.setBackground(Colors.getBackground());
        paneExtracts.setSelectionColor(Colors.getForeground());
        paneExtracts.setSelectedTextColor(Colors.getBackground());
        paneExtracts.setCaretColor(Colors.getBackground());
    }

    void refresh() {
        //refresh renderer before model
        renderer.refresh();
        model.refresh();
        setColors();
    }


    void setColumnFoundExtractsVisible(boolean visible) {
        if (visible) {
            //don't add the column many times
            if (columnModel.getColumnCount() < ResultColumn.values().length) {
                columnModel.addColumn(tcFoundExtracts);
                adjustColumnWidths();
            }
        } else {
            if (columnModel.getColumnCount() == ResultColumn.values().length) {
                columnModel.removeColumn(tcFoundExtracts);
                adjustColumnWidths();
            }
        }
    }
}
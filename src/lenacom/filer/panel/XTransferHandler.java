package lenacom.filer.panel;

import lenacom.filer.action.Actions;

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class XTransferHandler extends TransferHandler {
    private XTableImpl xTable;

    XTransferHandler(XTableImpl xTable) {
        this.xTable = xTable;
    }

    @Override
    public boolean canImport(TransferHandler.TransferSupport support) {
        return support.isDrop() && support.getTransferable().isDataFlavorSupported(DataFlavor.javaFileListFlavor);
    }

    @Override
    public boolean importData(TransferHandler.TransferSupport support) {
        if (!canImport(support)) {
            return false;
        }

        Transferable t = support.getTransferable();

        try {
            List<File> files = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
            //our copy/move functions expect that all source paths have the same parent
            Map<Path, List<Path>> parentToChildren = new HashMap<>();
            for (File file: files) {
                Path child = file.toPath();
                Path parent = child.getParent();
                List<Path> children = parentToChildren.get(parent);
                if (children == null) {
                    children = new ArrayList<>();
                    parentToChildren.put(parent, children);
                }
                children.add(child);
            }

            for (Path parent : parentToChildren.keySet()) {
                List<Path> children = parentToChildren.get(parent);
                if (parent.equals(xTable.getContext().getDirectory())) continue;
                Path[] source = children.toArray(new Path[children.size()]);

                if ((support.getDropAction() & support.getSourceDropActions()) == MOVE) {
                    Actions.getMoveAction().move(source, xTable);
                } else if ((support.getDropAction() & support.getSourceDropActions()) == COPY) {
                    Actions.getCopyAction().copy(source, xTable);
                }
            }
        } catch (UnsupportedFlavorException e) {
            return false;
        } catch (IOException e) {
            return false;
        }

        return true;
    }

    @Override
    public int getSourceActions(JComponent c) {
        return COPY_OR_MOVE;
    }

    @Override
    public Transferable createTransferable(JComponent c) {
        PathRow[] rows = xTable.getSelectedRows();
        if ((rows.length == 0 || rows[0] == xTable.getParentRow()) || xTable.getContext().isZip()) rows = new PathRow[0];
        return new XTransferable(rows);
    }

    private final class XTransferable implements Transferable {
        private List<File> files;

        XTransferable(PathRow[] rows) {
            files = new ArrayList<>(rows.length);
            for (PathRow row: rows) {
                files.add(row.getPath().toFile());
            }
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[] {DataFlavor.javaFileListFlavor};
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return flavor == DataFlavor.javaFileListFlavor && files.size() > 0;
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            return files;
        }
    }
}

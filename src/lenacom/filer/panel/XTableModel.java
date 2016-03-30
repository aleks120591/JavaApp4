package lenacom.filer.panel;

import lenacom.filer.config.Configuration;
import lenacom.filer.config.Constants;
import lenacom.filer.message.Errors;
import lenacom.filer.path.NativePathWithAttributes;
import lenacom.filer.path.PathUtils;
import lenacom.filer.zip.ExtractedTmpFile;
import lenacom.filer.zip.ZipPathProcessor;
import lenacom.filer.zip.ZipWorker;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

class XTableModel extends AbstractTableModel implements DirectoryRowListener {
    private List<XTableModelCell[]> cells = Collections.emptyList();
    private ParentDirectoryRow parentRow = null;
    private List<XTableListener> listeners = new ArrayList<>();
    private SwingWorker watcher;

    private Path directory;
    private Map<Path, Path> tmpZipParent = new HashMap<>();
    private ZipWorker zipWorker;

    private enum Mode {NATIVE, ZIP}
    private Mode mode = Mode.NATIVE;

    void setContextDirectory(Path newDir) {
        assert(SwingUtilities.isEventDispatchThread());

        Path oldDir = directory;
        try {
            tryToSetContextDirectory(newDir);
        } catch (Exception e) {
            Errors.showError(e);
            if (oldDir != null && !newDir.equals(oldDir)) {
                setContextDirectory(oldDir);
            } else {
                Path defaultRoot = PathUtils.getDefaultRoot();
                if (!newDir.equals(defaultRoot)) {
                    setContextDirectory(defaultRoot);
                }
            }
        }
    }

    private void tryToSetContextDirectory(Path newDirectory) throws IOException {

        if (!PathUtils.existsNoFollowLink(newDirectory)) {
            ZipPathProcessor processor = null;
            if (zipWorker != null) processor = zipWorker.getPathProcessor();
            //zip subdir or 2d level zip or not existent path
            if (processor != null && processor.directoryExists(newDirectory)) {
                //existent zip subdir
            } else if (PathUtils.isZip(newDirectory) && processor != null && processor.fileExists(newDirectory)) {
                //2d level zip
                ExtractedTmpFile etf = zipWorker.extractTempFile(newDirectory);
                etf.watchChangesAndUpdateZipImmediately();
                Path tmpZip = etf.getFile();
                tmpZipParent.put(tmpZip, newDirectory.getParent());
                newDirectory = tmpZip;
                mode = Mode.ZIP;
            } else {
                //not existent path
                newDirectory = PathUtils.getClosestExistentParent(newDirectory);
            }
        }

        if (PathUtils.existsNoFollowLink(newDirectory)) { //native dir or zip
            if (PathUtils.isDirectory(newDirectory)) {
                //native directory
                mode = Mode.NATIVE;
            } else if (PathUtils.isZip(newDirectory)) {
                //zip file
                if (zipWorker == null || !zipWorker.getZip().equals(newDirectory)) {
                    //only if going down the directory tree
                    try {
                        zipWorker = ZipWorker.createAndRead(newDirectory);
                    } catch (Exception e) {
                        Errors.showError(e);
                        zipWorker = null;
                        return;
                    }
                }
                mode = Mode.ZIP; //set mode after reading zip file which may cause Exception
            } else {
                //file but not zip, usually an exceptional situation
                newDirectory = newDirectory.getParent();
                mode = Mode.NATIVE;
            }
        } else if (PathUtils.isZip(newDirectory) || zipWorker != null) {
            mode = Mode.ZIP;
        }

        if (mode == Mode.NATIVE) {
            setNativeDirectory(newDirectory);
            zipWorker = null;
        }
        else {
            setZipDirectory(newDirectory);
        }

        //notify listeners after setting directory which may cause IOException
        int i = 0;
        XTableContext context = getContextDirectory();
        while (i < listeners.size()) {
            //use this.directory, it may be changed in setNativeDirectory()
            listeners.get(i++).workingDirectoryChanged(context);
        }
    }

    void refresh() {
        if (mode == Mode.ZIP) {
            try {
                zipWorker = ZipWorker.createAndRead(zipWorker.getZip());
            } catch (IOException e) {
                zipWorker = null;
                mode = Mode.NATIVE;
            }
        }
        setContextDirectory(this.directory);
    }

    void addListener(XTableListener l) {
        listeners.add(l);
    }

    void removeListener(XTableListener l) {
        listeners.remove(l);
    }

    void update(Path path) {
        assert(SwingUtilities.isEventDispatchThread());
        int rowIndex = getRowIndexByPath(path);
        if (rowIndex >= 0) {
            PathRow row = checkHiddenAndCreateNativeRow(path);
            if (row != null) {
                XTableModelCell[] rowCells = getAllRowCells(row);
                cells.set(rowIndex, rowCells);
                fireTableRowsUpdated(rowIndex, rowIndex);
                int i = 0;
                while (i < listeners.size()) {
                    listeners.get(i++).pathModified(path);
                }
            }
        }
    }

    void insert(Path path) {
        assert(SwingUtilities.isEventDispatchThread());
        PathRow row = checkHiddenAndCreateNativeRow(path);
        if (row != null) {
            XTableModelCell[] rowCells = getAllRowCells(row);
            int rowIndex = cells.size();
            cells.add(rowCells);
            fireTableRowsInserted(rowIndex, rowIndex);
            int i = 0;
            while (i < listeners.size()) listeners.get(i++).pathCreated(path);
        }
    }

    void delete(Path path) {
        assert(SwingUtilities.isEventDispatchThread());
        int rowIndex = getRowIndexByPath(path);
        if (rowIndex >= 0) {
            cells.remove(rowIndex);
            fireTableRowsDeleted(rowIndex, rowIndex);
            int i = 0;
            while (i < listeners.size()) listeners.get(i++).pathDeleted(path);
        }
    }

    XTableContext getContextDirectory() {
        return new XTableContext(directory, mode == Mode.ZIP? zipWorker : null);
    }

    ParentDirectoryRow getParentRow() {
        return parentRow;
    }

    private final static DirectoryStream.Filter<Path> notHiddenPaths = new DirectoryStream.Filter<Path>() {
        public boolean accept(Path path) throws IOException {
            return !Files.isHidden(path);
        }
    };

    private void setNativeDirectory(Path newDirectory) throws IOException {
        Path newParent = newDirectory.getParent();
        ParentDirectoryRow newParentRow = null;
        List<XTableModelCell[]> newCells = new ArrayList<>();
        if (newParent != null) {
            newParentRow = new ParentDirectoryRow(new NativePathWithAttributes(newParent));
            newCells.add(getAllRowCells(newParentRow));
        }

        long startTime = System.currentTimeMillis();
        boolean startedWaiting = false;
        try (DirectoryStream<Path> stream =
                     Configuration.getBoolean(Configuration.SHOW_HIDDEN_FILES, Boolean.FALSE)?
                     Files.newDirectoryStream(newDirectory) :
                     Files.newDirectoryStream(newDirectory, notHiddenPaths)
            ) {
            for (Path path: stream) {
                PathRow row = createNativeRow(path);
                if (row != null) newCells.add(getAllRowCells(row));
                if (!startedWaiting && ((System.currentTimeMillis() - startTime) > Constants.WAIT_DELAY)) {
                    for (XTableListener listener: listeners) listener.startWaiting();
                    startedWaiting = true;
                }
            }
        }

        if (watcher != null) watcher.cancel(true);
        this.directory = newDirectory;
        this.parentRow = newParentRow;
        this.cells = newCells;
        this.fireTableDataChanged();
        watcher = new DirectoryWatcher(this);
        watcher.execute();
    }

    private void setZipDirectory(Path newDirectory) throws IOException {
        List<XTableModelCell[]> newCells = new ArrayList<>();
        ParentDirectoryRow newParentRow;
        Path zip = zipWorker.getZip();
        Path newParent;
        if (newDirectory == zip) {
            newParent = tmpZipParent.get(newDirectory);
            if (newParent == null) newParent = newDirectory.getParent();
        } else {
            newParent = newDirectory.getParent();
            if (newParent == null) newParent = zip;
        }
        ZipPathProcessor pp = zipWorker.getPathProcessor();
        if (pp.directoryExists(newParent)) {
            newParentRow = new ParentDirectoryRow(newParent, pp);
        } else {
            newParentRow = new ParentDirectoryRow(new NativePathWithAttributes(newParent));
        }
        newCells.add(getAllRowCells(newParentRow));

        Set<Path> children = zipWorker.getChildren(newDirectory);
        //children can be null for an absolutely empty zip
        if (children != null) {
            for (Path path : children) {
                PathRow row = createRow(path, zipWorker.getPathProcessor());
                newCells.add(getAllRowCells(row));
            }
        }

        if (watcher != null) watcher.cancel(true);
        this.directory = newDirectory;
        this.parentRow = newParentRow;
        this.cells = newCells;
        this.fireTableDataChanged();
        watcher = new ZipWatcher(this, zip);
        watcher.execute();
    }

    //can return null
    private PathRow createNativeRow(Path path) {
        PathRow row = null;
        try {
            NativePathWithAttributes pwa = new NativePathWithAttributes(path);

            if (pwa.isDirectory()) {
                row = new DirectoryRow(pwa);
                ((DirectoryRow) row).addListener(this);
            } else {
                row = new FileRow(pwa);
            }
        } catch (java.nio.file.NoSuchFileException e) {
            //it's OK, do nothing
        } catch (IOException e) {
            e.printStackTrace();
        }
        return row;
    }

    private PathRow checkHiddenAndCreateNativeRow(Path path) {
        PathRow row = null;
        try {
            if (Configuration.getBoolean(Configuration.SHOW_HIDDEN_FILES, Boolean.FALSE) || !Files.isHidden(path)) {
                 row = createNativeRow(path);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return row;
    }

    private PathRow createRow(Path path, ZipPathProcessor processor) {
        PathRow row;
        if (processor.isDirectory(path)) {
            row = new DirectoryRow(path, processor);
            ((DirectoryRow) row).addListener(this);
        } else {
            row = new FileRow(path, processor);
        }
        return row;
    }

    private XTableModelCell[] getAllRowCells(PathRow row) {
        XTableModelCell[] rowCells = new XTableModelCell[XColumn.values().length];
        for (XColumn col : XColumn.values()) {
            rowCells[col.ordinal()] = XTableModelCell.create(row, col);
        }
        return rowCells;
    }

    private int getRowIndexByPath(Path path) {
        for (int i = 0; i < cells.size(); i++) {
            Path compareWithPath = cells.get(i)[XColumn.NAME.ordinal()].getRow().getPath();
            if (path.equals(compareWithPath)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int getRowCount() {
        return cells.size();
    }

    @Override
    public int getColumnCount() {
        return XColumn.values().length;
    }

    @Override
    public Object getValueAt(int rowIndex, int colIndex) {
        return cells.get(rowIndex)[colIndex];
    }

    @Override
    public void sizeModified(DirectoryRow row) {
        for (int i = 0; i < cells.size(); i++) {
            XTableModelCell[] rowCells = cells.get(i);
            PathRow compareWithRow = rowCells[XColumn.SIZE.ordinal()].getRow();
            if (compareWithRow == row) {
                XTableModelCell sizeCell = XTableModelCell.create(compareWithRow, XColumn.SIZE);
                rowCells[XColumn.SIZE.ordinal()] = sizeCell;
                fireTableRowsUpdated(i, i);
                for (XTableListener listener: listeners) {
                    listener.pathModified(row.getPath());
                }
                break;
            }
        }
    }

    @Override
    public Class<? extends Object> getColumnClass(int c) {
        return XTableModelCell.class;
    }
}

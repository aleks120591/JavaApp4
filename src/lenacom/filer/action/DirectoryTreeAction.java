package lenacom.filer.action;

import lenacom.filer.component.OkCancelDialog;
import lenacom.filer.config.XIcon;
import lenacom.filer.config.Configuration;
import lenacom.filer.config.Constants;
import lenacom.filer.panel.XPanel;
import lenacom.filer.panel.XTable;
import lenacom.filer.panel.XTableContext;
import lenacom.filer.path.PathUtils;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.List;

class DirectoryTreeAction extends XAction {

    public DirectoryTreeAction(String key, boolean withEllipsis) {
        super(key, withEllipsis);
        this.setIcon(XIcon.TREE.getIcon());
    }

    @Override
    public void act() {
        new DirectoryTreeDialog(getPanel());
    }

    private final static class DirectoryTreeDialog extends OkCancelDialog {
        private XTable activeTable;
        private JTree tree;
        private long startTime;
        private boolean startedWaiting = false;

        private DirectoryTreeDialog(XPanel xpnl) {
            super(xpnl, "dlg.dir.tree.title");
            this.activeTable = xpnl.getTable();

            startLongOperation();

            tree = new JTree(new DirTreeModel());
            tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
            tree.setRootVisible(false);
            tree.setEditable(false);

            tree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
                @Override
                public void valueChanged(TreeSelectionEvent e) {
                    setOkEnabled(tree.getSelectionModel().getSelectionCount() > 0);
                }
            });

            tree.addTreeExpansionListener(new TreeExpansionListener() {
                @Override
                public void treeExpanded(TreeExpansionEvent event) {
                    stopWaiting();
                }

                @Override
                public void treeCollapsed(TreeExpansionEvent event) {
                    //do nothing
                }
            });

            openCurrentPath();            

            this.setCenterComponent(new JScrollPane(tree));
            setVisibleRelativeTo(activeTable.getWrapper(), 1.0, 1.0);

            stopWaiting();
        }

        private void startLongOperation() {
            startedWaiting = false;
            startTime = System.currentTimeMillis();
        }

        private void startWaitingAfterDelay() {
            if (!startedWaiting && ((System.currentTimeMillis() - startTime) > Constants.WAIT_DELAY)) {
                startedWaiting = true;
                DirectoryTreeDialog.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                if (DirectoryTreeDialog.this.isVisible()) {
                    DirectoryTreeDialog.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                } else {
                    activeTable.getWrapper().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                }
            }
        }

        private void stopWaiting() {
            startedWaiting = false;
            DirectoryTreeDialog.this.setCursor(Cursor.getDefaultCursor());
            activeTable.getWrapper().setCursor(Cursor.getDefaultCursor());
        }

        private void openCurrentPath() {
            XTableContext context = activeTable.getContext();
            Path path = context.getClosestNativePath();
            List<Object> directoryItems = new ArrayList<>();
            Path parent = path;
            while (parent != null) {
                directoryItems.add(0, new DirectoryItem(parent));
                parent = parent.getParent();
            }
            directoryItems.add(0, tree.getModel().getRoot());
            TreePath treePath = new TreePath(directoryItems.toArray());
            tree.expandPath(treePath);
            tree.getSelectionModel().addSelectionPath(treePath);
            tree.scrollPathToVisible(treePath);
        }

        @Override
        protected boolean onOk() {
            Path path = ((DirectoryItem) tree.getSelectionPath().getLastPathComponent()).getDirectory();
            activeTable.setContextDirectory(path);
            return true;
        }

        private final class DirTreeModel extends DefaultTreeModel {
            private Map<DirectoryItem, List<DirectoryItem>> parentToChildren = new HashMap<>();
            private List<DirectoryItem> rootChildren;
            private static final String ROOT = "root";


            private DirTreeModel() {
                super(new DefaultMutableTreeNode(ROOT));
            }

            @Override
            public int getChildCount(Object parent) {
                return parent == root? getRootChildren().size() :
                        getChildren((DirectoryItem) parent).size();
            }

            @Override
            public boolean isLeaf(Object node) {
                if (getRootChildren().contains(node)) return false;
                return getChildCount(node) == 0;
            }

            @Override
            public Object getChild(Object parent, int index) {
                return parent == root? getRootChildren().get(index) :
                        getChildren((DirectoryItem) parent).get(index);
            }

            private List<DirectoryItem> getRootChildren() {
                if (rootChildren == null) {
                    rootChildren = new ArrayList<>();
                    for (Path child : FileSystems.getDefault().getRootDirectories()) {
                        rootChildren.add(new DirectoryItem(child));
                    }
                }
                return rootChildren;
            }

            private final DirectoryStream.Filter<Path> directories = new DirectoryStream.Filter<Path>() {
                public boolean accept(Path path) throws IOException {
                    return PathUtils.isDirectory(path);
                }
            };

            private final DirectoryStream.Filter<Path> notHiddenDirectories = new DirectoryStream.Filter<Path>() {
                public boolean accept(Path path) throws IOException {
                    return PathUtils.isDirectory(path) && !Files.isHidden(path);
                }
            };

            private List<DirectoryItem> createChildren(DirectoryItem parent) {
                startLongOperation();
                List<DirectoryItem> children = new ArrayList<>();
                if (parent == null) {
                    for (Path child : FileSystems.getDefault().getRootDirectories()) {
                        children.add(new DirectoryItem(child));
                    }
                } else {
                    Path path = parent.getDirectory();
                    try (DirectoryStream<Path> stream =
                                 Configuration.getBoolean(Configuration.SHOW_HIDDEN_FILES, Boolean.FALSE)?
                                         Files.newDirectoryStream(path, directories) :
                                         Files.newDirectoryStream(path, notHiddenDirectories)
                    ) {
                        for (Path child: stream) {
                            children.add(new DirectoryItem(child));
                            startWaitingAfterDelay();
                        }
                    } catch (IOException | DirectoryIteratorException x) {
                        x.printStackTrace();
                    }
                }
                Collections.sort(children);
                return children;
            }

            private List<DirectoryItem> getChildren(DirectoryItem parent) {
                List<DirectoryItem> children = parentToChildren.get(parent);
                if (children == null) {
                    children = createChildren(parent);
                    parentToChildren.put(parent, children);
                }
                return children;
            }
        }
    }

    private static final class DirectoryItem extends DefaultMutableTreeNode implements Comparable<DirectoryItem> {
        private Path directory;
        private String value;
        private String sortValue;

        private DirectoryItem(Path directory) {
            super(directory);
            this.directory = directory;
            value = PathUtils.getName(directory);
            sortValue = toString().toLowerCase();
        }

        public Path getDirectory() {
            return directory;
        }

        public String toString() {
            return value;
        }

        String getSortValue() {
            return sortValue;
        }

        @Override
        public int compareTo(DirectoryItem directoryItem) {
            return sortValue.compareTo(directoryItem.getSortValue());
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof DirectoryItem) {
                return directory.equals(((DirectoryItem) obj).getDirectory());
            }
            return super.equals(obj);
        }

        @Override
        public int hashCode() {
            return directory.hashCode();
        }
    }
}

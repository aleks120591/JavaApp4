package lenacom.filer.action;

import lenacom.filer.component.ListDialog;
import lenacom.filer.config.XIcon;
import lenacom.filer.message.Messages;
import lenacom.filer.panel.XTable;
import lenacom.filer.path.Roots;

import javax.swing.event.ListSelectionEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class RootsAction extends XAction {

    public RootsAction(String key, boolean withEllipsis) {
        super(key, withEllipsis);
        this.setIcon(XIcon.HOME.getIcon());
    }

    @Override
    public void act() {
        new RootsDialog(getTable());
    }

    private final static class RootsDialog extends ListDialog<Path> {

        private XTable table;

        RootsDialog(XTable table) {
            super(table.getWrapper(), "dlg.roots.title");

            this.table = table;

            List<Path> data = Roots.getRoots();
            Path currentRoot = table.getContext().getDirectory().getRoot();
            setList(data.toArray(new Path[data.size()]), currentRoot);

            this.setVisibleRelativeToParent();
        }

        @Override
        protected boolean onOk() {
            Path root = getSelectedValue();
            if (!Files.isReadable(root)) {
                Messages.showMessage(RootsDialog.this, "dlg.roots.err.root.not.readable", root);
                return false;
            }
            table.setContextDirectory(root);
            return true;
        }

        @Override
        public void valueChanged(ListSelectionEvent e) {
            //do nothing
        }
    }
}

package lenacom.filer.action;

import lenacom.filer.component.ListDialog;
import lenacom.filer.panel.XColumn;
import lenacom.filer.panel.XTable;

import javax.swing.event.ListSelectionEvent;

class SortAction extends XAction {

    public SortAction(String key, boolean withEllipsis) {
        super(key, withEllipsis);
    }

    @Override
    public void act() {
        new SortDialog(getTable());
    }

    private final static class SortDialog extends ListDialog<XColumn> {
        private XTable table;
        public SortDialog(XTable xtbl) {
            super(xtbl.getWrapper(), "dlg.sort.title");
            this.table = xtbl;
            setList(xtbl.getColumns(), xtbl.getSortColumn());
            setVisibleRelativeToParent();
        }

        @Override
        protected boolean onOk() {
            table.setSortColumn(getSelectedValue());
            return true;
        }

        @Override
        public void valueChanged(ListSelectionEvent e) {
            //do nothing
        }
    }
}

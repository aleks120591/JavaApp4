package lenacom.filer.component;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public abstract class ListDialog<E> extends OkCancelDialog implements ListSelectionListener {
    private static final int ROWS = 10;
    private JList<E> list;

    public ListDialog(Component owner, String key) {
        super(owner, key);
    }

    protected void setList(E[] data) {
        setList(data, data.length > 0? data[0] : null);
    }

    protected void setList(E[] data, E selectedValue) {
        list = new JList<>(data);
        list.setCellRenderer(new XListCellRenderer<E>());
        list.setSelectedValue(selectedValue, true);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setVisibleRowCount(Math.min(ROWS, data.length));

        JPanel pnlCenter = new JPanel(new GridLayout(1, 1));
        pnlCenter.add(new JScrollPane(list));
        this.setCenterComponent(pnlCenter);

        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    if (onOk()) dispose();
                }
            }
        });

        list.addListSelectionListener(this);
        list.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                E selectedValue = list.getSelectedValue();
                setOkEnabled(selectedValue != null);
            }
        });
        this.setResizable(true);
        this.setOkEnabled(list.getSelectedValue() != null);
    }

    protected E getSelectedValue() {
        return list.getSelectedValue();
    }

    @Override
    public abstract void valueChanged(ListSelectionEvent e);
}
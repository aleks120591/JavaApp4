package lenacom.filer.action;

import lenacom.filer.component.*;
import lenacom.filer.config.ResourceKey;
import lenacom.filer.path.PathUtils;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

abstract class ConfirmPathsDialog extends OkCancelDialog {
    private static final int ROWS = 10;
    private HtmlLabel lblMessage;

    ConfirmPathsDialog(Component owner, ResourceKey keyTitle, ResourceKey keyMessage, Path... paths) {
        super(owner, keyTitle, "btn.yes", "btn.no");
        List<String> names = new ArrayList<>(paths.length);
        for (Path path: paths) names.add(PathUtils.getName(path));
        Collections.sort(names);

        lblMessage = new HtmlLabel(keyMessage);

        JList<Object> list = new JList<>(names.toArray());
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setVisibleRowCount(Math.min(ROWS, paths.length));
        list.setCellRenderer(new XListCellRenderer<>());

        JPanel pnlCenter = new JPanel(new XBorderLayout());
        pnlCenter.add(lblMessage, BorderLayout.NORTH);
        pnlCenter.add(new JScrollPane(list), BorderLayout.CENTER);
        this.setCenterComponent(pnlCenter);
    }

    @Override
    public void pack() {
        super.pack();
        int diff = lblMessage.adjustHeight();
        this.setHeight(getHeight() + diff);
    }
}

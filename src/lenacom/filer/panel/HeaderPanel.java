package lenacom.filer.panel;

import lenacom.filer.component.ButtonMenu;
import lenacom.filer.component.HtmlPane;
import lenacom.filer.config.Bookmarks;
import lenacom.filer.config.Colors;
import lenacom.filer.config.Fonts;
import lenacom.filer.config.Resources;
import lenacom.filer.menu.DoButtonMenu;
import lenacom.filer.menu.GoButtonMenu;
import lenacom.filer.menu.HeaderContextMenu;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.List;

class HeaderPanel extends JPanel implements Bookmarks.BookmarksListener {
    private static final String TOOLTIP_TEMPLATE = Resources.getMessage("header.tooltip.template");
    private BreadcrumsPane breadcrumsPane;
    private XPanel xpnl;
    private ButtonMenu btnGoMenu, btnDoMenu;

    HeaderPanel(XPanel xpnl) {
        super(new BorderLayout(0, 0));
        this.xpnl = xpnl;

        XTable table = xpnl.getTable();

        breadcrumsPane = new BreadcrumsPane();
        table.addListener(new XTableAdapter() {
            @Override
            public void workingDirectoryChanged(XTableContext wd) {
                breadcrumsPane.update(wd.getDirectory());
            }
        });

        breadcrumsPane.update(table.getContext().getDirectory());

        breadcrumsPane.addMouseListener(new HeaderMouseListener());

        btnGoMenu = new GoButtonMenu(xpnl);
        btnDoMenu = new DoButtonMenu(xpnl);

        btnGoMenu.setFocusable(false);
        btnDoMenu.setFocusable(false);
        breadcrumsPane.setFocusable(false);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        buttonsPanel.add(btnDoMenu);
        buttonsPanel.add(btnGoMenu);

        add(buttonsPanel, BorderLayout.WEST);
        add(new JScrollPane(breadcrumsPane), BorderLayout.CENTER);

        Bookmarks.addListener(this);
    }

    void showGoMenu() {
        btnGoMenu.doClick();
    }

    void showDoMenu() {
        btnDoMenu.doClick();
    }

    @Override
    public void bookmarksChanged(List<Bookmarks.Bookmark> bookmarks) {
        refresh();
    }

    void refresh() {
        breadcrumsPane.update(xpnl.getTable().getContext().getDirectory());
    }

    private class BreadcrumsPane extends HtmlPane {
        private static final String HTML_HEADER = "<html><head><style>body '{'background-color: {0}; color: {1}; margin: 2px; font-family: {2}; font-size: {3} pt'}'\n" +
                "a '{'color: {1}; font-family: {2}; font-size: {3} pt'}'</style></head><body>";
        private static final String LINK_FORMAT = "<a href=\"{0}\">{1}</a>";
        private static final String HTML_FOOTER = "</body></html>";


        private BreadcrumsPane() {
            this.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        }

        private String getHtmlHeader(boolean isBookmark) {
            Font font = Fonts.getFont();
            return MessageFormat.format(HTML_HEADER,
                    isBookmark? Colors.colorToHex(Colors.getBookmarkBackground()) : Colors.colorToHex(Colors.getBackground()),
                    isBookmark? Colors.colorToHex(Colors.getBookmarkForeground()) : Colors.colorToHex(Colors.getForeground()),
                    font.getFamily(),
                    font.getSize()
            );
        }

        @Override
        protected void onHyperlinkClicked(HyperlinkEvent e) {
            xpnl.getTable().setContextDirectory(Paths.get(e.getDescription()));
        }

        @Override
        protected String getToolTip(HyperlinkEvent e) {
            return MessageFormat.format(TOOLTIP_TEMPLATE, e.getDescription());
        }

        private void update(Path path) {
            StringBuilder sb = new StringBuilder();
            boolean isBookmark = Bookmarks.getBookmarkByPath(path) != null;
            sb.append(getHtmlHeader(isBookmark));

            if (path != null) {
                Path[] crums = new Path[path.getNameCount() + 1];
                crums[0] = path.getRoot();
                for (int i = 0; i < path.getNameCount(); i++) {
                    crums[i + 1] = path.getName(i);
                }

                Path href = null;
                for (int i = 0; i < crums.length; i++) {
                    String name = crums[i].toString();
                    if (href == null) href = crums[i];
                    else href = href.resolve(crums[i]);
                    if (name.endsWith(File.separator)) name = name.substring(0, name.length() - 1);
                    String link = MessageFormat.format(LINK_FORMAT, href.toAbsolutePath().toString(), name);
                    sb.append(link).append(File.separator);
                }
            }

            sb.append(HTML_FOOTER);
            breadcrumsPane.setText(sb.toString());
            repaint();
        }
    }

    private class HeaderMouseListener extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            processMouseEvent(e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            processMouseEvent(e);
        }

        private void processMouseEvent(MouseEvent e) {
            //popup trigger depends on look and feel
            if (e.isPopupTrigger()) {
                JPopupMenu menu = HeaderContextMenu.getMenu(xpnl);
                if (menu != null) menu.show(breadcrumsPane, e.getX(), e.getY());
            }
        }
    }
}
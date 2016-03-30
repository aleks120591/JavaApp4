package lenacom.filer.action.text;

import lenacom.filer.component.XMenu;
import lenacom.filer.component.XMenuItem;
import lenacom.filer.config.Charsets;
import lenacom.filer.config.XIcon;
import lenacom.filer.config.Configuration;
import lenacom.filer.config.Resources;

import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.nio.charset.Charset;

class TextTopMenu extends JMenuBar implements ActionListener{
    private JMenuItem miFind, miReplace, miUndo, miRedo, miCopy,
            miCut, miPaste, miSelectAll, miCharset;
    private JCheckBoxMenuItem miWrapLines;
    private JRadioButtonMenuItem miNewLineCRLF, miNewLineLF, miNewLineCR;
    private FileDialog dlgFile;
    private JMenu menuDo, menuSettings;

    TextTopMenu(FileDialog dlgFile, boolean editable) {
        this.dlgFile = dlgFile;
        menuDo = getMenuDo(editable);
        menuSettings = getMenuSettings();
        add(menuDo);
        add(menuSettings);
    }

    JMenu getMenuDo() {
        return menuDo;
    }

    private JMenu getMenuDo(boolean editable) {
        JMenu menu = new XMenu("dlg.file.menu.do");
        menu.addMenuListener(new MenuListener() {
            @Override
            public void menuSelected(MenuEvent event) {
                FileArea fileArea = dlgFile.getFileArea();
                if (miUndo != null) miUndo.setEnabled(fileArea.canUndo());
                if (miRedo != null) miRedo.setEnabled(fileArea.canRedo());
                String selectedText = fileArea.getSelectedText();

                boolean canCopy = selectedText != null && !selectedText.isEmpty();
                miCopy.setEnabled(canCopy);
                if (miCut != null) miCut.setEnabled(canCopy);

                if (miPaste != null) {
                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    boolean canPaste = true;
                    try {
                        canPaste = clipboard.getData(DataFlavor.stringFlavor) != null;
                    } catch (UnsupportedFlavorException | IOException e) {
                        //do nothing
                    }
                    miPaste.setEnabled(canPaste);
                }
            }

            @Override
            public void menuDeselected(MenuEvent e) {
                //do nothing
            }

            @Override
            public void menuCanceled(MenuEvent e) {
                //do nothing
            }
        });

        miFind = addMenuItem(menu, "dlg.file.menu.do.find", true, KeyEvent.VK_F);
        miFind.setIcon(XIcon.FIND.getIcon());
        if (editable) miReplace = addMenuItem(menu, "dlg.file.menu.do.replace", true, KeyEvent.VK_R);

        menu.add(new JSeparator());
        miCopy = addMenuItem(menu, "dlg.file.menu.do.copy", KeyEvent.VK_C);
        miCopy.setIcon(XIcon.COPY.getIcon());
        if (editable) {
            miCut = addMenuItem(menu, "dlg.file.menu.do.cut", KeyEvent.VK_X);
            miCut.setIcon(XIcon.CUT.getIcon());
            miPaste = addMenuItem(menu, "dlg.file.menu.do.paste", KeyEvent.VK_V);
        }
        miSelectAll = addMenuItem(menu, "dlg.file.menu.do.select.all", KeyEvent.VK_A);

        if (editable) {
            menu.add(new JSeparator());
            miUndo = addMenuItem(menu, "dlg.file.menu.do.undo", KeyEvent.VK_Z);
            miUndo.setIcon(XIcon.UNDO.getIcon());
            miRedo = addMenuItem(menu, "dlg.file.menu.do.redo", KeyEvent.VK_Y);
            miRedo.setIcon(XIcon.REDO.getIcon());
        }

        return menu;
    }

    private JMenu getMenuSettings() {
        JMenu menu = new XMenu("dlg.file.menu.settings");
        menu.setIcon(XIcon.SETTINGS.getIcon());

        boolean wrapLines = Configuration.getBoolean(Configuration.WRAP_LINES, Boolean.TRUE);
        String keyText = "dlg.file.menu.settings.wrap.lines";
        miWrapLines = new JCheckBoxMenuItem(Resources.getMessage(keyText), wrapLines);
        Character mnemonic = Resources.getMnemonic(keyText);
        if (mnemonic != null) miWrapLines.setMnemonic(mnemonic);
        miWrapLines.addActionListener(this);

        for (Charset charset: Charsets.getCharsets()) {
            JMenuItem item = new JMenuItem(charset.displayName());
            item.addActionListener(this);
            menu.add(item);
        }
        miCharset = addMenuItem(menu, "dlg.file.menu.settings.charset", true);
        menu.add(new JSeparator());
        menu.add(miWrapLines);
        menu.add(getMenuLineSeparator());

        return menu;
    }

    private JMenu getMenuLineSeparator() {
        JMenu menu = new XMenu("dlg.file.menu.settings.line.separator");

        LineSeparator lineSeparator = LineSeparator.getCurrentLineSeparator();
        miNewLineLF = createRadioButtonMenuItem("dlg.file.menu.settings.line.separator.lf", lineSeparator == LineSeparator.LF);
        miNewLineCR = createRadioButtonMenuItem("dlg.file.menu.settings.line.separator.cr", lineSeparator == LineSeparator.CR);
        miNewLineCRLF = createRadioButtonMenuItem("dlg.file.menu.settings.line.separator.crlf", lineSeparator == LineSeparator.CRLF);

        ButtonGroup bg = new ButtonGroup();
        bg.add(miNewLineLF);
        bg.add(miNewLineCR);
        bg.add(miNewLineCRLF);

        menu.add(miNewLineLF);
        menu.add(miNewLineCR);
        menu.add(miNewLineCRLF);

        return menu;
    }

    private JRadioButtonMenuItem createRadioButtonMenuItem(String keyLabel, boolean selected) {
        JRadioButtonMenuItem mi = new JRadioButtonMenuItem(Resources.getMessage(keyLabel), selected);
        mi.addActionListener(this);
        return mi;
    }

    private JMenuItem addMenuItem(JMenu parent, String keyText, int keyCode) {
        return addMenuItem(parent, keyText, false, keyCode);
    }

    private JMenuItem addMenuItem(JMenu parent, String keyText, boolean withEllipsis) {
        JMenuItem item = new XMenuItem(keyText, withEllipsis);
        item.addActionListener(this);
        parent.add(item);
        return item;
    }

    private JMenuItem addMenuItem(JMenu parent, String keyText, boolean withEllipsis, int keyCode) {
        JMenuItem item = addMenuItem(parent, keyText, withEllipsis);
        item.setAccelerator(KeyStroke.getKeyStroke(keyCode, InputEvent.CTRL_DOWN_MASK));
        return item;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (miFind == source) {
            dlgFile.showFindPanelOrFind();
        } else if (miReplace == source) {
            dlgFile.showReplacePanel();
        } else if (miUndo == source) {
            dlgFile.getFileArea().undo();
        } else if (miRedo == source) {
            dlgFile.getFileArea().redo();
        } else if (miCopy == source) {
            dlgFile.getFileArea().copy();
        } else if (miCut == source) {
            dlgFile.getFileArea().cut();
        } else if (miPaste == source) {
            dlgFile.getFileArea().paste();
        } else if (miSelectAll == source) {
            dlgFile.getFileArea().requestFocusInWindow();
            dlgFile.getFileArea().selectAll();
        } else if (miCharset == source) {
            dlgFile.showChangeCharsetDialog();
        } else if (miWrapLines == source) {
            boolean lineWrap = miWrapLines.isSelected();
            dlgFile.getFileArea().setLineWrap(lineWrap);
            Configuration.setBoolean(Configuration.WRAP_LINES, lineWrap);
        } else if (miNewLineLF == source) {
            LineSeparator.setCurrentLineSeparator(LineSeparator.LF);
        } else if (miNewLineCR == source) {
            LineSeparator.setCurrentLineSeparator(LineSeparator.CR);
        } else if (miNewLineCRLF == source) {
            LineSeparator.setCurrentLineSeparator(LineSeparator.CRLF);
        } else {
            //charset item
            String text = ((JMenuItem) source).getText();
            for (Charset charset: Charsets.getCharsets()) {
                if (charset.displayName().equals(text)) {
                    dlgFile.changeCharset(charset);
                }
            }
        }
    }
}
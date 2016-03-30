package lenacom.filer.action;

import lenacom.filer.component.*;
import lenacom.filer.config.XIcon;
import lenacom.filer.config.Configuration;
import lenacom.filer.config.ResourceKey;
import lenacom.filer.message.Errors;
import lenacom.filer.message.Messages;
import lenacom.filer.panel.XTable;
import lenacom.filer.panel.XTableContext;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;

class NewFileAction extends XAction {

    public NewFileAction(String key, boolean withEllipsis) {
        super(key, withEllipsis);
        this.setIcon(XIcon.NEW_FILE.getIcon());
    }

    @Override
    public void act() {
        new NewFileDialog(getTable());
    }

    private static final class NewFileDialog extends NewPathDialog implements ActionListener {
        private static final String DEFAULT_EXTENSIONS = "txt;html";
        private static final int EXTENSION_MAX_LENGTH = 20;
        private Path newFile;
        private JButton btnCreateAndEdit;
        private java.util.List<ExtensionButton> extensionButtons = new ArrayList<>();
        private String extensions;
        private JPanel pnlCenter;
        private JPanel pnlExtensions;

        NewFileDialog(XTable xtbl) {
            super(xtbl, "dlg.new.file.title");

            this.setJMenuBar(getTopMenu());

            if (xtbl.getContext().isZip()) {
                this.addRightButtons(btnCreate, btnCancel);
            } else {
                btnCreateAndEdit = XButton.create("dlg.new.file.btn.create.and.edit");
                btnCreateAndEdit.addActionListener(this);
                registerButton(btnCreateAndEdit, KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK);
                btnCreateAndEdit.setToolTipText("Ctrl-E");
                onTextChanged();
                this.addRightButtons(btnCreate, btnCreateAndEdit, btnCancel);
            }

            pnlCenter = new JPanel(new XBorderLayout());
            pnlCenter.add(LabelComponentPanel.createHorizontal("dlg.new.file.name", textField), BorderLayout.CENTER);

            extensions = Configuration.getString(Configuration.NEW_FILE_EXTENSIONS);
            if (extensions == null) extensions = DEFAULT_EXTENSIONS;
            addExtensionButtons();

            this.setCenterComponent(pnlCenter);
            setVisibleRelativeToParent(1.0, null);
        }

        private void initExtensionButtons() {
            extensionButtons.clear();
            String[] names = extensions.split(";");
            for (String name: names) {
                if (name.length() > EXTENSION_MAX_LENGTH) {
                    name = name.substring(0, EXTENSION_MAX_LENGTH);
                }
                name = name.trim();
                if (!name.isEmpty()) {
                    ExtensionButton btn = new ExtensionButton("." + name);
                    extensionButtons.add(btn);
                }
            }
        }

        private void addExtensionButtons() {
            initExtensionButtons();
            if (extensionButtons.size() > 0) {
                if (pnlExtensions != null) pnlCenter.remove(pnlExtensions);
                pnlExtensions = new HorizontalPanel(extensionButtons.toArray(new JButton[extensionButtons.size()]));
                pnlCenter.add(pnlExtensions, BorderLayout.SOUTH);
            }
        }

        private JMenuBar getTopMenu() {
            JMenuItem miExtensions = new XMenuItem("dlg.new.file.menu.extensions");
            miExtensions.addActionListener(this);

            JMenu menuSettings = new XMenu("dlg.new.file.menu.settings");
            menuSettings.setIcon(XIcon.SETTINGS.getIcon());
            menuSettings.add(miExtensions);

            JMenuBar topMenu = new JMenuBar();
            topMenu.add(menuSettings);
            return topMenu;
        }

        @Override
        protected void onTextChanged() {
            super.onTextChanged();
            if (btnCreateAndEdit != null) {
                btnCreateAndEdit.setEnabled(!getText().isEmpty());
            }
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Object source = e.getSource();
            if (btnCreateAndEdit == source) {
                if (onCreate()) {
                    //dispose first to focus the edit file dialog
                    dispose();
                    Actions.getEditAction().edit(newFile);
                }
            } else if (source instanceof ExtensionButton) {
                String text = ((ExtensionButton) source).getText();
                insertText(text);
            } else if (source instanceof JMenuItem) {
                new ExtensionConfigureDialog();
            }
        }

        private void createFile(Path path) throws IOException {
            XTableContext context = xtbl.getContext();
            if (context.isZip()) {
                context.getZipWorker().addFile(path);
            } else {
                Files.createFile(path);
            }
        }

        @Override
        protected boolean onCreate() {
            XTableContext context = xtbl.getContext();
            try {
                String newFileName = getText();
                newFile = context.getDirectory().resolve(newFileName);
            } catch (InvalidPathException e) {
                Messages.showMessage(NewFileDialog.this, "dlg.new.file.err.invalid.name");
                return false;
            }

            if (context.getPathProcessor().fileExists(newFile)) {
                Messages.showMessage(NewFileDialog.this, "err.path.already.exists", newFile);
                return false;
            }

            try {
                createFile(newFile);
                return true;
            } catch (IOException e) {
                Errors.showError(NewFileDialog.this, e);
                return false;
            }
        }

        private class ExtensionButton extends JButton {
            private ExtensionButton(String text) {
                super(text);
                this.addActionListener(NewFileDialog.this);
                if (text.equalsIgnoreCase(".txt")) {
                    registerButton(this, KeyEvent.VK_T, InputEvent.CTRL_DOWN_MASK);
                    setToolTipText("Ctrl-T");
                }
            }
        }

        private class ExtensionConfigureDialog extends TextDialog {

            ExtensionConfigureDialog() {
                super(NewFileDialog.this,
                        new ResourceKey("dlg.new.file.ext.title"),
                        new ResourceKey("dlg.new.file.ext.lbl.name"),
                        new ResourceKey("dlg.new.file.ext.lbl.descr"),
                        extensions);
                setVisibleRelativeToParent();
            }

            @Override
            protected boolean onOk() {
                extensions = getText();
                Configuration.setString(Configuration.NEW_FILE_EXTENSIONS, extensions);
                addExtensionButtons();
                NewFileDialog.this.validate();
                return true;
            }
        }
    }
}

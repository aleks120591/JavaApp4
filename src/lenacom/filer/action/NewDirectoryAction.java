package lenacom.filer.action;

import lenacom.filer.component.LabelComponentPanel;
import lenacom.filer.component.XButton;
import lenacom.filer.config.XIcon;
import lenacom.filer.message.Errors;
import lenacom.filer.message.Messages;
import lenacom.filer.panel.XTable;
import lenacom.filer.panel.XTableContext;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

class NewDirectoryAction extends XAction {

    public NewDirectoryAction(String key, boolean withEllipsis) {
        super(key, withEllipsis);
        setIcon(XIcon.NEW_DIRECTORY.getIcon());
    }

    @Override
    public void act() {
        new NewDirectoryDialog(getTable());
    }

    private final static class NewDirectoryDialog extends NewPathDialog {
        private Path newDirectory;
        private JButton btnCreateAndOpen;

        NewDirectoryDialog(XTable xtbl) {
            super(xtbl, "dlg.new.dir.title");

            if (xtbl.getContext().isZip()) {
                this.addRightButtons(btnCreate, btnCancel);
            } else {
                btnCreateAndOpen = XButton.create("dlg.new.dir.btn.create.and.open");
                ActionListener listener = new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (onCreate()) {
                            dispose();
                            NewDirectoryDialog.this.xtbl.setContextDirectory(newDirectory);
                        }
                    }
                };
                btnCreateAndOpen.addActionListener(listener);
                registerButton(btnCreateAndOpen, KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK);
                btnCreateAndOpen.setToolTipText("Ctrl-O");
                onTextChanged();
                this.addRightButtons(btnCreate, btnCreateAndOpen, btnCancel);
            }

            this.setCenterComponent(LabelComponentPanel.createHorizontal("dlg.new.dir.name", textField));
            setVisibleRelativeToParent(1.0, null);
        }

        @Override
        protected void onTextChanged() {
            super.onTextChanged();
            if (btnCreateAndOpen != null) {
                btnCreateAndOpen.setEnabled(!getText().isEmpty());
            }
        }

        private void createDirectory(Path path) throws IOException {
            XTableContext context = xtbl.getContext();
            if (context.isZip()) {
                context.getZipWorker().addDirectory(path);
            } else {
                Files.createDirectory(path);
            }
        }

        @Override
        protected boolean onCreate() {
            XTableContext context = xtbl.getContext();

            try {
                newDirectory = context.getDirectory().resolve(getText());
            } catch (InvalidPathException e) {
                Messages.showMessage(NewDirectoryDialog.this, "dlg.new.dir.err.invalid.name");
                return false;
            }

            if (context.getPathProcessor().directoryExists(newDirectory)) {
                Messages.showMessage(NewDirectoryDialog.this, "err.path.already.exists", newDirectory);
                return false;
            }

            try {
                createDirectory(newDirectory);
                return true;
            } catch (IOException e) {
                Errors.showError(NewDirectoryDialog.this, e);
                return false;
            }
        }
    }
}

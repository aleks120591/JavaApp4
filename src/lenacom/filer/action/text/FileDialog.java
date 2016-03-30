package lenacom.filer.action.text;

import lenacom.filer.component.*;
import lenacom.filer.config.*;
import lenacom.filer.message.Confirmation;
import lenacom.filer.message.Errors;
import lenacom.filer.message.Messages;
import lenacom.filer.panel.XTableContext;
import lenacom.filer.path.PathUtils;
import lenacom.filer.root.ModelessDialogs;
import lenacom.filer.zip.ExtractedTmpFile;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

public class FileDialog extends AbstractDialog implements ActionListener, Refreshable {
    private ExtractedTmpFile tmpFile;
    private Path path;
    private FileArea fileArea;
    private FindReplacePanel pnlFindReplace;
    private PositionPanel position;
    private JLabel lblCharset;
    private JButton btnSave, btnSaveAndClose, btnClose;
    private JToggleButton btnMaximize;
    private boolean editable;
    private EncodedText encodedText;
    private XTableContext context;

    public FileDialog(Component parent, XTableContext context, Path path, String findText, boolean editable) throws IOException {
        this(parent, context, path, editable);
        showFindPanel(findText);
    }

    public FileDialog(Component parent, XTableContext context, Path path, boolean editable) throws IOException {
        super(parent);

        if (context.isZip()) {
            this.tmpFile = context.getZipWorker().extractTempFile(path);
            this.path = tmpFile.getFile();
        } else {
            this.path = path;
        }

        if (!PathUtils.existsFollowLink(this.path)) {
            Messages.showMessage(FileDialog.this, "err.path.does.not.exist", this.path);
            return;
        }

        this.context = context;
        this.editable = editable;
        init();
        if (editable && context.isZip() && this.tmpFile != null) {
            this.tmpFile.watchChanges();
        }

        ModelessDialogs.add(this);
    }

    private void init()throws IOException {
        if (!checkType(path) || !checkSize(path)) return;

        setTitle(new ResourceKey((editable ? "dlg.file.title.edit" : "dlg.file.title.view"), path));
        this.setModalityType(Dialog.ModalityType.MODELESS);

        TextTopMenu topMenu = new TextTopMenu(this, editable);
        this.setJMenuBar(topMenu);
        registerButton(topMenu.getMenuDo(), KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK);
        topMenu.getMenuDo().setToolTipText("Ctrl-D");

        encodedText = new EncodedText(path);
        initComponents();

        JPanel pnlMain = getMainPanel();
        pnlMain.setBorder(XEmptyBorder.create());
        this.setLayout(new GridLayout(1, 1, 0, 0));
        this.add(pnlMain);

        boolean maximize = Configuration.getBoolean(Configuration.MAXIMIZE_VIEW_EDIT, false);
        if (maximize) {
            maximize();
            setVisible(true);
        } else {
            setVisibleRelativeToParent(1.0, 1.0);
        }
        btnMaximize.setSelected(maximize);

        boolean wrapLines = Configuration.getBoolean(Configuration.WRAP_LINES, Boolean.TRUE);
        fileArea.setLineWrap(wrapLines);
        fileArea.requestFocus();
    }

    private boolean checkType(Path file) {
        String contentType = null;
        try {
            contentType = Files.probeContentType(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (contentType != null && !contentType.startsWith("text")) {
            if(!Confirmation.confirm(this,
                    new ResourceKey("dlg.file.confirm.check.file.type")
            ))
                return false;
        }
        return true;
    }

    private boolean checkSize(Path file) {
        long size = 0;
        try {
            size = Files.size(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (size > Constants.DEFAULT_TEXT_FILE_MAX_SIZE) {
            if(!Confirmation.confirm(this,
                    new ResourceKey("dlg.file.confirm.check.file.size")
            ))
                return false;
        }

        return true;
    }

    private void initComponents() {
        pnlFindReplace = new FindReplacePanel(this);
        pnlFindReplace.getFindDownButton().setToolTipText("Ctrl-F");
        pnlFindReplace.getFindUpButton().setToolTipText("Ctrl-Shift-F");
        pnlFindReplace.getClearButton().setToolTipText("Ctrl-L");
        pnlFindReplace.getHideButton().setToolTipText("Ctrl-I");
        registerButton(pnlFindReplace.getFindUpButton(), KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK);
        registerButton(pnlFindReplace.getClearButton(), KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK);
        registerButton(pnlFindReplace.getHideButton(), KeyEvent.VK_I, InputEvent.CTRL_DOWN_MASK);

        if (editable) {
            btnSave = this.createDefaultButton("dlg.file.btn.save");
            btnSave.setIcon(XIcon.SAVE.getIcon());
            btnSave.setEnabled(false);
            btnSave.addActionListener(this);
            registerButton(btnSave, KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK);
            btnSave.setToolTipText("Ctrl-S");

            btnSaveAndClose = XButton.create("dlg.file.btn.save.and.close");
            btnSaveAndClose.setEnabled(false);
            btnSaveAndClose.addActionListener(this);
            registerButton(btnSaveAndClose, KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK);
            btnSaveAndClose.setToolTipText("Ctrl-Shift-S");
        }

        btnClose = this.createCancelButton("btn.close");
        btnClose.setToolTipText("Ctrl-Q");
        btnClose.setIcon(XIcon.CLOSE.getIcon());
        registerButton(btnClose, KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK);

        btnMaximize = new MaximizeButton("dlg.file.btn.maximize", "dlg.file.btn.minimize");
        btnMaximize.addActionListener(this);
        btnMaximize.setToolTipText("Ctrl-M");
        registerButton(btnMaximize, KeyEvent.VK_M, InputEvent.CTRL_DOWN_MASK);

        fileArea = new FileArea(encodedText.getText(), editable);
        fileArea.setCaretPosition(0);
        fileArea.addCaretListener(new FileCaretListener());
        fileArea.getDocument().addDocumentListener(new EditableDocumentListener() {
            protected void onEveryUpdate() {
                if (btnSave != null) {
                    btnSave.setEnabled(true);
                    btnSaveAndClose.setEnabled(true);
                }
            }
        });

        position = new PositionPanel();
        position.setPosition(0, 0);

        lblCharset = new JLabel(encodedText.getCurrentCharset().name());
    }

    private JPanel getMainPanel() {
        JPanel pnlLeft = new HorizontalPanel(btnMaximize, position, lblCharset);
        JComponent buttons = editable? new HorizontalPanel(btnSave, btnSaveAndClose, btnClose) : btnClose;

        JPanel pnlSouth = new JPanel(new XBorderLayout());
        pnlSouth.add(pnlLeft, BorderLayout.CENTER);
        pnlSouth.add(buttons, BorderLayout.EAST);

        JPanel pnlMain = new JPanel(new XBorderLayout());
        pnlMain.add(pnlFindReplace, BorderLayout.NORTH);
        pnlMain.add(new JScrollPane(fileArea), BorderLayout.CENTER);
        pnlMain.add(pnlSouth, BorderLayout.SOUTH);

        return pnlMain;
    }

    @Override
    protected boolean onCancel() {
        final AtomicBoolean ok = new AtomicBoolean(true);
        if (editable) {
            if (btnSave.isEnabled()) {
                new YesNoCancelDialog(this, new ResourceKey("confirm.title"),
                        new ResourceKey("dlg.file.msg.save.changes", path)) {

                    @Override
                    protected void onYes() {
                        saveFile();
                        updateZip();
                    }

                    @Override
                    protected void onNo() {
                        //do nothing
                    }

                    @Override
                    protected boolean onCancel() {
                        ok.set(false);
                        return true;
                    }
                };
            } else {
                updateZipIfChanged();
            }
        }
        if (ok.get()) {
            onClose();
            return true;
        } else {
            return false;
        }
    }

    private void saveFile() {
        try {
            String text = fileArea.getText();
            text = LineSeparator.getCurrentLineSeparator().replaceLineSeparators(text);
            if (fileArea.isEditable()) encodedText.setText(text);
            Files.write(path, encodedText.getBytes());
        } catch (IOException e) {
            Errors.showError(FileDialog.this, e);
        }
    }

    private void updateZipIfChanged() {
        if (context.isZip() && this.tmpFile != null ) {
            this.tmpFile.updateZipIfChanged();
        }
    }

    private void updateZip() {
        if (context.isZip() && this.tmpFile != null ) {
            this.tmpFile.updateZip();
        }
    }

    private void onClose() {
        Charsets.addCharsetOnTop(encodedText.getCurrentCharset());
        Configuration.setBoolean(Configuration.MAXIMIZE_VIEW_EDIT, btnMaximize.isSelected());
        ModelessDialogs.remove(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (btnSave == source) {
            saveFile();
            btnSave.setEnabled(false);
            btnSaveAndClose.setEnabled(false);
        } if (btnSaveAndClose == source) {
            saveFile();
            updateZip();
            onClose();
            dispose();
        } else if (btnMaximize == source) {
            if (btnMaximize.isSelected()) {
                maximize();
            } else {
                setVisibleRelativeToParent(1.0, 1.0);
            }
            btnMaximize.requestFocus();
        }
    }

    @Override
    public void refresh() {
        fileArea.refresh();
        pnlFindReplace.refresh();
    }

    private class FileCaretListener implements CaretListener {
        @Override
        public void caretUpdate(CaretEvent event) {
            int pos = event.getDot();
            try {
                int rowIndex = fileArea.getLineOfOffset(pos);
                int colIndex = pos - fileArea.getLineStartOffset(rowIndex);
                position.setPosition(rowIndex + 1, colIndex + 1);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }
    }

    //TODO comments.txt to UTF-8
    void showChangeCharsetDialog() {
        if (editable) encodedText.setText(fileArea.getText());
        new CharsetDialog(this, encodedText.getBytes()) {
            @Override
            protected boolean onOk() {
                return changeCharset(getSelectedCharset());
            }
        };
    }

    boolean changeCharset(Charset newCharset) {
        encodedText.setCurrentCharset(newCharset);
        if (encodedText.getCurrentCharset().equals(newCharset)) {
            fileArea.setText(encodedText.getText());
            lblCharset.setText(encodedText.getCurrentCharset().name());
            return true;
        } else {
            Messages.showMessage(FileDialog.this, "dlg.file.err.failed.set.charset", newCharset);
            return false;
        }
    }

    FileArea getFileArea() {
        return fileArea;
    }

    void showFindPanel(String text) {
        pnlFindReplace.setVisibleAsFind(text);
    }

    void showFindPanelOrFind() {
        if (pnlFindReplace.isVisible()) {
            pnlFindReplace.getFindDownButton().doClick();
        } else {
            pnlFindReplace.setVisibleAsFind();
        }
    }

    void showReplacePanel() {
        pnlFindReplace.setVisibleAsReplace();
    }
}

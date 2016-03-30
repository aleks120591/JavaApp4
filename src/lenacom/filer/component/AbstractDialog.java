package lenacom.filer.component;

import lenacom.filer.config.ResourceKey;
import lenacom.filer.config.Resources;
import lenacom.filer.config.XIcon;
import lenacom.filer.util.KeyActionsUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public abstract class AbstractDialog extends JDialog implements Closable {
    private Component parent;

    public AbstractDialog(Component owner) {
        super(owner instanceof JDialog? (JDialog) owner :
            owner instanceof JComponent? (Window) SwingUtilities.getAncestorOfClass(Window.class, owner) :
            owner instanceof JFrame? (JFrame) owner:
            null);
        parent = owner;
        init();
    }

    @Override
    public void setTitle(String keyTitle) {
        setTitle(new ResourceKey(keyTitle));
    }

    public void setTitle(ResourceKey key) {
        super.setTitle(Resources.getMessage(key));
    }

    private void init() {
        this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (onCancel()) {
                    dispose();
                }
            }
        });

        this.setMaximumSize(Toolkit.getDefaultToolkit().getScreenSize());
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    }

    protected void registerActionListener(ActionListener listener, int keyCode, int modifiers) {
        KeyActionsUtils.registerActionListener(getRootPane(), listener, KeyStroke.getKeyStroke(keyCode, modifiers));
    }

    protected void registerButton(final AbstractButton button, int keyCode, int modifiers) {
        KeyActionsUtils.registerButton(getRootPane(), button, KeyStroke.getKeyStroke(keyCode, modifiers));
    }

    public void setVisibleRelativeToParent() {
        packAndSetVisibleRelativeTo(parent);
    }

    //pass null for a default width or height
    public void setVisibleRelativeToParent(Double percentOfWidth, Double percentOfHeight) {
        setVisibleRelativeTo(parent, percentOfWidth, percentOfHeight);
    }

    public void setVisibleRelativeTo(Component relativeTo) {
        packAndSetVisibleRelativeTo(relativeTo);
    }

    public void setVisibleRelativeTo(Component relativeTo, Double percentOfWidth, Double percentOfHeight) {
        pack();
        adjustPreferredSize(relativeTo, percentOfWidth, percentOfHeight);
        packAndSetVisibleRelativeTo(relativeTo);
    }

    private void adjustPreferredSize(Component relativeTo, Double percentOfWidth, Double percentOfHeight) {
        assert(percentOfWidth == null || (percentOfWidth > 0 && percentOfWidth <= 1));
        assert(percentOfHeight == null || (percentOfHeight > 0 && percentOfHeight <= 1));

        Dimension preferredSize = getSize();
        Dimension relativeToSize = relativeTo.getSize();
        if (percentOfWidth != null) {
            preferredSize.width = (int) Math.round(relativeToSize.width * percentOfWidth);
        }
        if (percentOfHeight != null) {
            preferredSize.height = (int) Math.round(relativeToSize.height * percentOfHeight);
        }
        super.setPreferredSize(preferredSize);
    }

    private void packAndSetVisibleRelativeTo(Component relativeTo) {
        pack();
        setLocationRelativeTo(relativeTo);
        this.requestFocusInWindow();
        setVisible(true);
    }

    @Override
    public void setPreferredSize(Dimension d) {
        Dimension max = Toolkit.getDefaultToolkit().getScreenSize();
        if (d.height > max.height) d.height = max.height;
        if (d.width > max.width) d.width = max.width;
        super.setPreferredSize(d);
    }

    public void maximize() {
        Window owner = getOwner();
        setPreferredSize(owner.getSize());
        pack();
        setLocationRelativeTo(owner);
    }

    protected abstract boolean onCancel();

    @Override
    public void close() {
        if (onCancel()) {
            dispose();
        }
    }

    protected JButton createCancelButton() {
        return createCancelButton("btn.cancel");
    }

    protected JButton createCancelButton(String key) {
        JButton cancel = XButton.create(new ResourceKey(key));
        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (onCancel()) {
                    dispose();
                }
            }
        });
        return cancel;
    }

    protected JButton createDefaultButton() {
        return createDefaultButton("btn.ok");
    }

    protected JButton createDefaultButton(String key) {
        JButton ok = XButton.create(new ResourceKey(key));
        getRootPane().setDefaultButton(ok);
        return ok;
    }

    protected void setHeight(int height) {
        Dimension size = getSize();
        size.height = height;
        this.setSize(size);
    }
}

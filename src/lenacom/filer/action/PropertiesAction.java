package lenacom.filer.action;

import lenacom.filer.component.*;
import lenacom.filer.config.*;
import lenacom.filer.message.Errors;
import lenacom.filer.panel.*;
import lenacom.filer.path.*;
import lenacom.filer.path.processor.SafePathVisitor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.DosFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.util.*;
import java.util.List;

//TODO* http://docs.oracle.com/javase/7/docs/api/javax/imageio/package-summary.html
class PropertiesAction extends XAction implements ContextAction {
    private static final Set<SelectionType> types = SelectionType.getAllExceptMultipleAndParentDirectory();

    @Override
    public Set<SelectionType> getSelectionTypes() {
        return types;
    }

    public PropertiesAction(String key, boolean withEllipsis) {
        super(key, withEllipsis);
        setIcon(XIcon.PROPERTIES.getIcon());
    }

    @Override
    public boolean isEnabled() {
        SelectionType type = SelectionType.getSelectionType(getTable());
        return types.contains(type);
    }

    @Override
    public void act() {
        new PropertiesDialog(getPanel());
    }

    private final class PropertiesDialog extends ButtonsDialog implements DirectoryRowListener {
        private XPanel xpnl;
        private PathRow row;
        private java.util.List<Attribute> attributes;
        private JCheckBox chbApplyToChildren;
        private JLabel lblSize;
        private JButton btnOk;

        public PropertiesDialog(XPanel xpnl) {
            super(xpnl);
            this.xpnl = xpnl;
            this.row = xpnl.getTable().getFocusedRow();
            setTitle(new ResourceKey("dlg.properties.title", row.getPath()));
            this.setModalityType(Dialog.ModalityType.MODELESS);

            try {
                init();
            } catch (IOException e) {
                Errors.showError(e);
                return;
            }

            if (attributes != null) {
                btnOk = this.createDefaultButton();
                btnOk.setEnabled(false);
                btnOk.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        PropertiesDialog.this.onOk();
                        PropertiesDialog.this.dispose();
                    }
                });
                this.addRightButtons(btnOk, this.createCancelButton());
            } else {
                this.addRightButtons(this.createCancelButton("btn.close"));
            }

            setVisibleRelativeToParent(1.0, null);
        }

        private JPanel getSizePanel(PathRow row) {
            JPanel pnlSize = new JPanel(new XBorderLayout());
            String formattedSize =
                    row.getSize() instanceof FormattedPathSize?
                    ((FormattedPathSize) row.getSize()).toStringWithBytes() :
                    row.getSize().toString();
            lblSize = new JLabel(formattedSize);
            pnlSize.add(lblSize, BorderLayout.CENTER);

            lblSize.setForeground(Color.BLACK);
            if (row instanceof DirectoryRow) {
                final DirectoryRow dirRow = (DirectoryRow) row;
                if (dirRow.canCalculateSize()) {
                    JButton btnDirSize = XButton.create("dlg.properties.btn.dir.size");
                    btnDirSize.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            XTableContext context = xpnl.getTable().getContext();
                            if (context.isZip()) {
                                DirectorySizeCalculator.calculate(context.getZipWorker(), dirRow);
                            } else {
                                DirectorySizeCalculator.calculate(dirRow);
                            }
                        }
                    });
                    pnlSize.add(btnDirSize, BorderLayout.EAST);
                    if (dirRow.isSizeApproximate())
                    lblSize.setForeground(dirRow.isSizeApproximate()? Color.GRAY : Color.BLACK);
                    dirRow.addListener(this);
                }
            }

            return pnlSize;
        }

        private JPanel getTargetPanel(final Path target) {
            JPanel pnlTarget = new JPanel(new XBorderLayout());
            JLabel lblTarget = new PathLabel(target);
            pnlTarget.add(lblTarget, BorderLayout.CENTER);
            if (PathUtils.existsNoFollowLink(target)) {
                JButton btnOpen = XButton.create("dlg.properties.btn.go.to.target");
                btnOpen.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        XPanels.getOtherPanel(xpnl).getTable().goTo(target);
                    }
                });
                pnlTarget.add(btnOpen, BorderLayout.EAST);
            } else {
                lblTarget.setForeground(Color.GRAY);
            }
            return pnlTarget;
        }

        @Override
        public void sizeModified(DirectoryRow row) {
            String formattedSize = row.getSize() instanceof FormattedPathSize?
                ((FormattedPathSize) row.getSize()).toStringWithBytes() : "";
            lblSize.setText(formattedSize);
            lblSize.setForeground(Color.BLACK);
        }

        private void init() throws IOException {
            Path path = row.getPath();

            LabelComponentTablePanel pnlBasic = new LabelComponentTablePanel();
            pnlBasic.addRow("dlg.properties.lbl.name", new PathLabel(path));
            String keyType = "dlg.properties.lbl." + (row.isSymlink()? "symlink." : "") + (row instanceof DirectoryRow?  "dir" : "file");
            pnlBasic.addRow("dlg.properties.lbl.type", keyType);
            if (row instanceof FileRow) {
                Charset charset = PathAttributes.getFileCharset(path);
                if (charset != null) {
                    pnlBasic.addRow("dlg.properties.lbl.charset", charset.name());
                }

                try {
                    String contentType = Files.probeContentType(path);
                    pnlBasic.addRow("dlg.properties.lbl.content.type", contentType);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (row.isSymlink()) {
                Path target = row.getSymlinkTarget();
                pnlBasic.addRow("dlg.properties.lbl.target", getTargetPanel(target));
            } else if (row.getSize() != null){
                pnlBasic.addRow("dlg.properties.lbl.size", getSizePanel(row));
            }

            if (PathUtils.existsNoFollowLink(path)) {
                BasicFileAttributes basicAttributes = Files.readAttributes(path, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
                pnlBasic.addRow("dlg.properties.lbl.created", PathUtils.formatDateExt(basicAttributes.creationTime()));
                pnlBasic.addRow("dlg.properties.lbl.last.accessed", PathUtils.formatDateExt(basicAttributes.lastAccessTime()));
            }
            pnlBasic.addRow("dlg.properties.lbl.last.modified", PathUtils.formatDateExt(row.getDate()));
            if (PathUtils.existsNoFollowLink(path)) {
                pnlBasic.addRow("dlg.properties.lbl.owner", Files.getOwner(path, LinkOption.NOFOLLOW_LINKS).getName());
            }

            JPanel pnlCenter = new JPanel(new XBorderLayout());
            pnlCenter.add(pnlBasic, BorderLayout.NORTH);

            if (PathUtils.existsNoFollowLink(path)) {
                DosFileAttributes dosAttributes = PathAttributes.getDosAttributes(path);
                if (dosAttributes != null) {
                    pnlCenter.add(getDosAttributesPanel(dosAttributes), BorderLayout.CENTER);
                } else {
                    Set<PosixFilePermission> posixAttributes = PathAttributes.getPosixAttributes(path);
                    if (posixAttributes != null) {
                        pnlCenter.add(getPosixAttributesPanel(posixAttributes), BorderLayout.CENTER);
                    }
                }
            }

            this.setCenterComponent(pnlCenter);
        }

        private JPanel getDosAttributesPanel(DosFileAttributes dosAttributes) {
            attributes = new ArrayList<>();
            for(DosAttribute da : DosAttribute.values()) {
                boolean selected = false;
                String title = "";
                switch(da) {
                    case ARCHIVE:
                        title = "dlg.properties.lbl.dos.archive";
                        selected = dosAttributes.isArchive();
                        break;
                    case HIDDEN:
                        title = "dlg.properties.lbl.dos.hidden";
                        selected = dosAttributes.isHidden();
                        break;
                    case READONLY:
                        title = "dlg.properties.lbl.dos.readonly";
                        selected = dosAttributes.isReadOnly();
                        break;
                    case SYSTEM:
                        title = "dlg.properties.lbl.dos.system";
                        selected = dosAttributes.isSystem();
                        break;
                }
                attributes.add(new Attribute(da, title, selected));
            }
            return getAttributesPanel(attributes, 2, 2);
        }

        private JPanel getPosixAttributesPanel(Set<PosixFilePermission> posixAttributes) {
            attributes = new ArrayList<>();
            for(PosixFilePermission pfa: PosixFilePermission.values()) {
                boolean selected = posixAttributes.contains(pfa);
                String title = "";
                switch(pfa) {
                    case OWNER_READ:
                        title = "dlg.properties.lbl.posix.owner.read";
                        break;
                    case OWNER_WRITE:
                        title = "dlg.properties.lbl.posix.owner.write";
                        break;
                    case OWNER_EXECUTE:
                        title = "dlg.properties.lbl.posix.owner.execute";
                        break;
                    case GROUP_READ:
                        title = "dlg.properties.lbl.posix.group.read";
                        break;
                    case GROUP_WRITE:
                        title = "dlg.properties.lbl.posix.group.write";
                        break;
                    case GROUP_EXECUTE:
                        title = "dlg.properties.lbl.posix.group.execute";
                        break;
                    case OTHERS_READ:
                        title = "dlg.properties.lbl.posix.others.read";
                        break;
                    case OTHERS_WRITE:
                        title = "dlg.properties.lbl.posix.others.write";
                        break;
                    case OTHERS_EXECUTE:
                        title = "dlg.properties.lbl.posix.others.execute";
                        break;
                }
                attributes.add(new Attribute(pfa, title, selected));
            }
            return getAttributesPanel(attributes, 3, 3);
        }

        private JPanel getAttributesPanel(final java.util.List<Attribute> attributes, int rows, int cols) {
            ActionListener listener = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    ((AttributeCheckbox) e.getSource()).update();
                    boolean enable = false;
                    for (Attribute a: attributes) {
                        if (a.hasChanged()) {
                            enable = true;
                            break;
                        }
                    }
                    btnOk.setEnabled(enable);
                }
            };

            JPanel pnlAttrs = new JPanel(new GridLayout(rows, cols, UIConstants.HORIZONTAL_GAP, UIConstants.VERTICAL_GAP));
            for (Attribute a: attributes) {
                AttributeCheckbox chb = new AttributeCheckbox(a);
                chb.addActionListener(listener);
                pnlAttrs.add(chb);
            }

            JPanel pnlContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            String attrs = Resources.getMessage("dlg.properties.lbl.attrs");
            pnlContainer.setBorder(BorderFactory.createTitledBorder(attrs));
            pnlContainer.add(pnlAttrs);

            JPanel pnl = new JPanel(new XBorderLayout());
            pnl.add(pnlContainer, BorderLayout.CENTER);

            if (row instanceof DirectoryRow) {
                chbApplyToChildren = new XCheckBox("dlg.properties.lbl.dos.apply.to.children", false);
                pnl.add(chbApplyToChildren, BorderLayout.SOUTH);
            }

            return pnl;
        }

        private boolean onOk() {
            if (attributes != null) {
                try {
                    Path path = row.getPath();
                    boolean applyToChildren = chbApplyToChildren != null && chbApplyToChildren.isSelected();
                    if (attributes.get(0).getValue() instanceof DosAttribute) {
                        //dos
                        for (Attribute a: attributes) {
                            if (a.hasChanged()) {
                                PathAttributes.setAttribute(path,
                                        ((DosAttribute) a.getValue()).getName(), a.isSelected());
                            }
                        }
                        if (applyToChildren) {
                            new ApplyToChildrenVisitor(path, attributes) {
                                @Override
                                protected void applyAttributes(Path path, List<Attribute> attributes) throws IOException {
                                    for (Attribute a: attributes) {
                                        PathAttributes.setAttribute(path, ((DosAttribute) a.getValue()).getName(), a.isSelected());
                                    }
                                }
                            }.execute();
                        }
                    } else {
                        //posix
                        final Set<PosixFilePermission> posixAttributes = new HashSet<>();
                        for (Attribute a: attributes) {
                            if (a.isSelected()) {
                                posixAttributes.add((PosixFilePermission) a.getValue());
                            }
                        }
                        PathAttributes.setPosixAttributes(path, posixAttributes);
                        if (applyToChildren) {
                            new ApplyToChildrenVisitor(path, attributes) {
                                @Override
                                protected void applyAttributes(Path path, List<Attribute> attributes) throws IOException {
                                    PathAttributes.setPosixAttributes(path, posixAttributes);
                                }
                            }.execute();
                        }
                    }
                } catch (IOException e) {
                    Errors.showError(PropertiesDialog.this, e);
                    return false;
                }
            }

            removeDirectoryListener();

            return true;
        }

        @Override
        protected boolean onCancel() {
            removeDirectoryListener();
            return true;
        }

        private void removeDirectoryListener() {
            if (row instanceof DirectoryRow) {
                DirectoryRow dirRow = (DirectoryRow) row;
                dirRow.removeListener(this);
            }
        }
    }

    private abstract class ApplyToChildrenVisitor extends SafePathVisitor {
        private java.util.List<Attribute> attributes;

        private ApplyToChildrenVisitor(Path dir, java.util.List<Attribute> attributes) {
            super(new Path[]{ dir });
            this.attributes = attributes;
        }

        @Override
        protected void safePostVisitDirectory(Path dir) throws Exception {
            applyAttributes(dir, attributes);
        }

        @Override
        protected void safePreVisitDirectory(Path dir, BasicFileAttributes attrs) throws Exception {
            //do nothing
        }

        @Override
        protected void safeVisitFile(Path file, BasicFileAttributes attr) throws Exception {
            applyAttributes(file, attributes);
        }

        protected abstract void applyAttributes(Path path, java.util.List<Attribute> attributes) throws IOException;
    }

    private class AttributeCheckbox extends JCheckBox {
        private Attribute attribute;

        private AttributeCheckbox(Attribute attribute) {
            this.attribute = attribute;
            this.setText(attribute.toString());
            this.setSelected(attribute.isSelected());
        }

        public void update() {
            attribute.setSelected(isSelected());
        }
    }

    private class Attribute {
        private Object value;
        private String title;
        private boolean wasOriginallySelected;
        private boolean selected;

        private Attribute(Object value, String keyTitle, boolean selected) {
            this.value = value;
            this.title = Resources.getMessage(keyTitle);
            this.wasOriginallySelected = selected;
            this.selected = selected;
        }

        private boolean isSelected() {
            return selected;
        }

        private void setSelected(boolean selected) {
            this.selected = selected;
        }

        private boolean hasChanged() {
            return selected != wasOriginallySelected;
        }

        @Override
        public String toString() {
            return title;
        }

        private Object getValue() {
            return value;
        }
    }

    private enum DosAttribute {
        ARCHIVE("dos:archive"),
        HIDDEN("dos:hidden"),
        READONLY("dos:readonly"),
        SYSTEM("dos:system");

        private String name;

        DosAttribute(String name) {
            this.name = name;
        }

        private String getName() {
            return name;
        }
    }

    private class PathLabel extends JLabel {
        private PathLabel(Path path) {
            super(path.toAbsolutePath().toString());
            addHierarchyListener(new HierarchyListener() {
                public void hierarchyChanged(HierarchyEvent e) {
                    if ((HierarchyEvent.SHOWING_CHANGED & e.getChangeFlags()) != 0
                            && isShowing()) {
                        //add a tooltip if needed first time after showing up
                        FontMetrics metrics = getFontMetrics(getFont());
                        if (metrics.stringWidth(getText()) > PathLabel.this.getWidth()) {
                            PathLabel.this.setToolTipText(getText());
                        }
                    }
                }
            });
        }
    }
}
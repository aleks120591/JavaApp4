package lenacom.filer.component;

import lenacom.filer.config.ResourceKey;

import javax.swing.*;
import java.awt.*;

public abstract class TextDialog extends OkCancelDialog {
    protected XTextField textField = new XTextField();
    private String initText;
    private HtmlLabel lblDescription;

    public TextDialog(Component owner, ResourceKey keyTitle, ResourceKey keyLabel, ResourceKey keyDescription) {
        this(owner, keyTitle, keyLabel, keyDescription, "");
    }

    public TextDialog(Component owner, ResourceKey keyTitle, ResourceKey keyLabel, ResourceKey keyDescription, String text) {
        super(owner, keyTitle);
        initTextField(text);

        JPanel pnlCenter = new JPanel(new GridBagLayout());
        lblDescription = new HtmlLabel(keyDescription);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = new Insets(0, 0, 0, 0);

        if (keyDescription != null) {
            pnlCenter.add(lblDescription, constraints);
            constraints.gridy++;
            constraints.weightx = 0;
            constraints.weighty = 0;
            constraints.fill = GridBagConstraints.BOTH;
            constraints.insets = new Insets(UIConstants.VERTICAL_GAP, 0, 0, 0);
        }
        pnlCenter.add(LabelComponentPanel.createHorizontal(keyLabel, textField), constraints);

        this.setCenterComponent(pnlCenter);
    }

    public TextDialog(Component owner, ResourceKey keyTitle, ResourceKey keyLabel) {
        this(owner, keyTitle, keyLabel, "");
    }

    public TextDialog(Component owner, ResourceKey keyTitle, ResourceKey keyLabel, String text) {
        super(owner, keyTitle);
        initTextField(text);
        this.setCenterComponent(LabelComponentPanel.createHorizontal(keyLabel, textField));
    }

    protected void initTextField(String text) {
        textField.setText(text);
        initText = text;
        textField.getDocument().addDocumentListener(new EditableDocumentListener() {
            @Override
            protected void onEveryUpdate() {
                String text = getText();
                setOkEnabled(!text.isEmpty() && !text.equals(initText));
            }
        });
        setOkEnabled(false);
    }

    public void setText(String text) {
        textField.setText(text);
    }

    protected String getText() {
        return textField.getText();
    }

    @Override
    public void setVisibleRelativeTo(Component relativeTo) {
        this.setVisibleRelativeTo(relativeTo, 1.0, null);
    }

    @Override
    public void pack() {
        super.pack();
        if (lblDescription != null) {
            int diff = lblDescription.adjustHeight();
            this.setHeight(getHeight() + diff);
        }
    }

}

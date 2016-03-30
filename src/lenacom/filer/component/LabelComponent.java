package lenacom.filer.component;

import lenacom.filer.config.ResourceKey;

import javax.swing.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class LabelComponent {
    private JLabel label;
    private JComponent component;

    public LabelComponent(ResourceKey keyLabel, JComponent component, String tooltip) {
        this(XLabel.createWithTooltipAndColon(keyLabel, tooltip), component);
    }

    public LabelComponent(ResourceKey keyLabel, JComponent component, ResourceKey keyTooltip) {
        this(XLabel.createWithTooltipAndColon(keyLabel, keyTooltip), component);
    }


    public LabelComponent(String keyLabel, JComponent component) {
        this(new ResourceKey(keyLabel), component);
    }

    public LabelComponent(ResourceKey keyLabel, JComponent component) {
        this(XLabel.createWithColon(keyLabel), component);
    }

    private LabelComponent(JLabel label, final JComponent component) {
        this.label = label;
        label.setLabelFor(component);
        this.component = component;

        component.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if ("enabled".equals(evt.getPropertyName())) {
                    boolean enabled = LabelComponent.this.component.isEnabled();
                    LabelComponent.this.label.setEnabled(enabled);
                }
            }
        });

        component.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                LabelComponent.this.label.setVisible(true);
            }
            @Override
            public void componentHidden(ComponentEvent e) {
                LabelComponent.this.label.setVisible(false);
            }
        });
    }

    public JLabel getLabel() {
        return label;
    }

    public JComponent getComponent() {
        return component;
    }
}

package lenacom.filer.component;

import lenacom.filer.config.ResourceKey;

import javax.swing.*;
import java.awt.*;

public class LabelComponentPanel extends JPanel {

    public static LabelComponentPanel createHorizontal(String keyLabel, JComponent component) {
        return new LabelComponentPanel(new ResourceKey(keyLabel), component, false);
    }

    public static LabelComponentPanel createVertical(String keyLabel, JComponent component) {
        return new LabelComponentPanel(new ResourceKey(keyLabel), component, true);
    }

    public static LabelComponentPanel createVertical(String keyLabel, JComponent component, String keyTooltip) {
        return new LabelComponentPanel(new ResourceKey(keyLabel), component, true, new ResourceKey(keyTooltip));
    }

    public static LabelComponentPanel createHorizontal(ResourceKey keyLabel, final JComponent component) {
        return new LabelComponentPanel(keyLabel, component, false);
    }

    public static LabelComponentPanel createVertical(ResourceKey keyLabel, final JComponent component) {
        return new LabelComponentPanel(keyLabel, component, true);
    }

    private LabelComponentPanel(ResourceKey keyLabel, JComponent component, boolean vertical) {
        this(new LabelComponent(keyLabel, component), vertical);
    }

    private LabelComponentPanel(ResourceKey keyLabel, JComponent component, boolean vertical, ResourceKey keyTooltip) {
        this(new LabelComponent(keyLabel, component, keyTooltip), vertical);
    }

    private LabelComponentPanel(LabelComponent labelComponent, boolean vertical) {
        super(new XBorderLayout());
        this.add(labelComponent.getLabel(), vertical? BorderLayout.NORTH : BorderLayout.WEST);
        this.add(labelComponent.getComponent(), BorderLayout.CENTER);
    }
}

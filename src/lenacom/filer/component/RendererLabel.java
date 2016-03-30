package lenacom.filer.component;

import javax.swing.*;
import java.awt.*;

public class RendererLabel extends JLabel {

    public RendererLabel() {
        setOpaque(true);
        setHorizontalAlignment(SwingConstants.LEFT);
    }

    //overridden for performance reasons
    @Override
    public void invalidate() {}
    @Override
    public void validate() {}
    @Override
    public void revalidate() {}
    @Override
    public void repaint(long tm, int x, int y, int width, int height) {}
    @Override
    public void repaint(Rectangle r) {}
    @Override
    public void repaint() {}
    @Override
    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {}
    @Override
    public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {}
}

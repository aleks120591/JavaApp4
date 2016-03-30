package lenacom.filer.component;

import javax.swing.*;
import java.awt.*;

public class XSplitPane extends JSplitPane{
    {
        setContinuousLayout(true);
        setDividerSize(2);
    }

    public XSplitPane(int newOrientation, Component newLeftComponent, Component newRightComponent) {
        super(newOrientation, newLeftComponent, newRightComponent);
    }
}

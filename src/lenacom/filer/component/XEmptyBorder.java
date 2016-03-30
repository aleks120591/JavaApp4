package lenacom.filer.component;

import javax.swing.*;
import javax.swing.border.Border;

public class XEmptyBorder {

    public static Border create() {
        return BorderFactory.createEmptyBorder(UIConstants.BORDER_GAP, UIConstants.BORDER_GAP, UIConstants.BORDER_GAP, UIConstants.BORDER_GAP);
    }
}

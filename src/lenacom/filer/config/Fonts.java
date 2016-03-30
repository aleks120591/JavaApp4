package lenacom.filer.config;

import lenacom.filer.component.RendererLabel;

import java.awt.*;

public class Fonts {
    private static Font font;
    static {
        String family = Configuration.getString(Configuration.FONT_FAMILY);
        Integer size = Configuration.getInteger(Configuration.FONT_SIZE);
        if (family != null && size != null) {
            font = new Font(family, Font.PLAIN, size > 0? size : 12);
        } else {
            font = new RendererLabel().getFont();
        }
    }

    public static Font getFont() {
        return font;
    }

    public static void setFont(Font font) {
        Fonts.font = font;
        Configuration.setString(Configuration.FONT_FAMILY, font.getFamily());
        Configuration.setInteger(Configuration.FONT_SIZE, font.getSize());
    }
}

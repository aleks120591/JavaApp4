package lenacom.filer.config;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class Colors {
    private static final String HEX_COLOR = "#%02x%02x%02x";

    private static boolean EYES_FRIENDLY_MODE;
    private static boolean EXTENSION_COLORS;

    private static int MIN_COLOR_INDEX, MAX_COLOR_INDEX, RANGE, K;
    private static Map<String, Color> extToColor = new HashMap<>();
    private static Color DARK_GREEN = new Color(0, 100, 0);

    static {
        Boolean eyesFriendlyMode = Configuration.getBoolean(Configuration.EYES_FRIENDLY_MODE);
        Boolean extensionColors = Configuration.getBoolean(Configuration.EXTENSION_COLORS);
        Colors.EYES_FRIENDLY_MODE = eyesFriendlyMode == null? true : eyesFriendlyMode;
        Colors.EXTENSION_COLORS = extensionColors == null? true : extensionColors;
        update();
    }

    public static boolean getEyesFriendlyMode() {
        assert(SwingUtilities.isEventDispatchThread());
        return Colors.EYES_FRIENDLY_MODE;
    }

    public static void setEyesFriendlyMode(boolean eyesFriendlyMode) {
        assert(SwingUtilities.isEventDispatchThread());
        if (Colors.EYES_FRIENDLY_MODE != eyesFriendlyMode) {
            Colors.EYES_FRIENDLY_MODE = eyesFriendlyMode;
            Configuration.setBoolean(Configuration.EYES_FRIENDLY_MODE, eyesFriendlyMode);
            update();
        }
    }

    public static boolean getExtensionColors() {
        assert(SwingUtilities.isEventDispatchThread());
        return Colors.EXTENSION_COLORS;
    }

    public static void setExtensionColors(boolean extensionColors) {
        assert(SwingUtilities.isEventDispatchThread());
        if (Colors.EXTENSION_COLORS != extensionColors) {
            Colors.EXTENSION_COLORS = extensionColors;
            Configuration.setBoolean(Configuration.EXTENSION_COLORS, extensionColors);
        }
    }

    private static void update() {
        extToColor.clear();
        if (Colors.EYES_FRIENDLY_MODE) {
            MIN_COLOR_INDEX = 8;
            MAX_COLOR_INDEX = 127 - 64;
        } else {
            MIN_COLOR_INDEX = 128 + 64 + 8;
            MAX_COLOR_INDEX = 255 - 8;
        }
        RANGE = MAX_COLOR_INDEX - MIN_COLOR_INDEX;
        K = (int) Math.floor(RANGE/26); //26 - count of letters in English alphabet
    }

    private static int getColorIndex(char ch) {
        assert(SwingUtilities.isEventDispatchThread());
        return MIN_COLOR_INDEX + (ch * K) % RANGE;
    }

    private static Color getColorByExtension(String extension) {
        assert(SwingUtilities.isEventDispatchThread());
        if (extension != null && !extension.isEmpty()) {
            String lowerExtension = extension.toLowerCase();
            Color color = extToColor.get(lowerExtension);
            if (color == null) {
                char[] chars = lowerExtension.toCharArray();
                int r = getColorIndex(chars[0]);
                int g = MAX_COLOR_INDEX, b = MAX_COLOR_INDEX;
                if (chars.length > 1) g = getColorIndex(chars[1]);
                if (chars.length > 2) b = getColorIndex(chars[2]);
                color = new Color(r, g, b);
                extToColor.put(lowerExtension, color);
            }
            return color;
        } else {
            return null;
        }
    }

    public static Color getBackgroundByExtension(String extension) {
        if (!Colors.EXTENSION_COLORS) return getBackground();
        Color background = getColorByExtension(extension);
        return background == null? getBackground() : background;
    }

    public static Color getBackground() {
        assert(SwingUtilities.isEventDispatchThread());
        return EYES_FRIENDLY_MODE ? Color.BLACK : Color.WHITE;
    }

    public static Color getForeground() {
        assert(SwingUtilities.isEventDispatchThread());
        return EYES_FRIENDLY_MODE ? Color.WHITE : Color.BLACK;
    }

    public static Color getDisabledForeground() {
        return EYES_FRIENDLY_MODE? Color.LIGHT_GRAY : Color.GRAY;
    }

    public static Color getListItemForeground(boolean selected) {
        return EYES_FRIENDLY_MODE? selected? Color.BLACK : Color.WHITE :
                selected? Color.WHITE : Color.BLACK;
    }

    public static Color getListItemBackground(boolean selected) {
        return EYES_FRIENDLY_MODE? selected? Color.WHITE : Color.BLACK :
                selected? Color.BLACK : Color.WHITE;
    }

    public static Color getSelectedForeground() {
        return getSelectedForeground(true);
    }

    public static Color getSelectedForeground(boolean active) {
        return EYES_FRIENDLY_MODE? active? Color.MAGENTA : Color.CYAN :
                active? Color.RED : Color.BLUE;
    }

    public static Color getBookmarkForeground() {
        return EYES_FRIENDLY_MODE? Color.WHITE : DARK_GREEN;
    }

    public static Color getBookmarkBackground() {
        return EYES_FRIENDLY_MODE? DARK_GREEN : Color.WHITE;
    }

    public static String colorToHex(Color color) {
        return String.format(HEX_COLOR, color.getRed(), color.getGreen(), color.getBlue());
    }

    public static Color getHighlightColor() {
        return EYES_FRIENDLY_MODE? Color.RED : Color.YELLOW;
    }

}

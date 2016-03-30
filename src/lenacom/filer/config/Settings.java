package lenacom.filer.config;

public class Settings {
    private static boolean showAttributes = true;

    static {
        showAttributes = Configuration.getBoolean(Configuration.SHOW_ATTRIBUTES, Boolean.FALSE);
    }

    public static void toggleShowAttribute() {
        showAttributes = !showAttributes;
        Configuration.setBoolean(Configuration.SHOW_ATTRIBUTES, showAttributes);
    }

    public static boolean isShowAttributes() {
        return showAttributes;
    }
}

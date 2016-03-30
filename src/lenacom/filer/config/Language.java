package lenacom.filer.config;

public enum Language {
    ENGLISH("en", "lang.english"),
    RUSSIAN("ru", "lang.russian");

    private String code, key, name;

    private Language(String code, String key) {
        this.code = code;
        this.key = key;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        if (name == null) {
            name = Resources.getMessage(key);
        }
        return name;
    }

    public String toString() {
        return getName();
    }

    public static Language getLanguage() {
        Language lang = null;
        String code = Configuration.getString(Configuration.LANGUAGE);
        if (code != null) {
            lang = code.equals(ENGLISH.code)? ENGLISH : RUSSIAN;
        }
        if (lang == null) lang = Language.ENGLISH;
        return lang;
    }

    public static void setLanguage(Language lang) {
        Configuration.setString(Configuration.LANGUAGE, lang.getCode());
    }
}

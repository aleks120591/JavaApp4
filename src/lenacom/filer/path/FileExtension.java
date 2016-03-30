package lenacom.filer.path;

import java.util.EnumMap;

public enum FileExtension {
    NAME,
    EXTENSION;

    public static EnumMap<FileExtension, String> getFileExtension(String name) {
        String ext = "";
        int pos = name.lastIndexOf('.');
        if (pos > 0) {
            ext = name.substring(pos + 1);
            name = name.substring(0, pos);
        }
        EnumMap<FileExtension, String> result = new EnumMap<>(FileExtension.class);
        result.put(NAME, name);
        result.put(EXTENSION, ext);
        return result;
    }
}

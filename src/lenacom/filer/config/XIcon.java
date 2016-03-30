package lenacom.filer.config;

import javax.swing.*;

public enum XIcon {
    BACK("back.png"),
    BOOKMARK("bookmark.png"),
    BROOM("broom.png"),
    CLOSE("close.png"),
    COPY("copy.png"),
    CURRENT("current.png"),
    CUT("cut.png"),
    DELETE("delete.png"),
    DELETE_BOOKMARK("delete_bookmark.png"),
    DOWN("down.png"),
    HAMMER("hammer.png"),
    HAND("hand.png"),
    HOME("home.png"),
    EDIT("edit.png"),
    FILER16("filer16.png"),
    FILER32("filer32.png"),
    FIND("find.png"),
    FORWARD("forward.png"),
    MAXIMIZE("maximize.png"),
    MINIMIZE("minimize.png"),
    MOVE("move.png"),
    NEW_DIRECTORY("new_directory.png"),
    NEW_FILE("new_file.png"),
    PANELS("panels.png"),
    PROPERTIES("properties.png"),
    REDO("redo.png"),
    REFRESH("refresh.png"),
    RENAME("rename.png"),
    SAVE("save.png"),
    SELECT("select.png"),
    SETTINGS("settings.png"),
    SYMLINK("symlink.png"),
    TEXT("text.png"),
    TREE("tree.png"),
    UNDO("undo.png"),
    UP("up.png"),
    ZIP("zip.png");

    private String fileName;
    private ImageIcon icon;

    private XIcon(String fileName) {
        this.fileName = fileName;
    }

    public ImageIcon getIcon() {
        if (icon == null) {
            icon = new ImageIcon(XIcon.class.getResource("icons/" + fileName));
        }
        return icon;
    }
}

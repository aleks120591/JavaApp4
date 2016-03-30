package lenacom.filer.action;

import lenacom.filer.panel.XPanels;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.Collection;

//TODO* batch file process: encoding, replace, rename
public class Actions {
    private static XAction BACK;
    private static BookmarkAction BOOKMARK;
    private static XAction CHANGE_ACTIVE_PANEL;
    private static XAction COMPARE_DIRECTORIES;
    private static XAction CONTEXT_MENU;
    private static CopyAction COPY;
    private static CopyDirectoryAction COPY_DIRECTORY;
    private static CopyPathAction COPY_PATH;
    private static DeleteAction DELETE;
    private static XAction DIRECTORY_AS_TEXT;
    private static DirectorySizeAction DIRECTORY_SIZE;
    private static XAction DIRECTORY_TREE;
    private static XAction DO;
    private static EditAction EDIT;
    private static BooleanAction EXTENSION_COLORS;
    private static BooleanAction EYES_FRIENDLY_MODE;
    private static XAction FIND;
    private static XAction FONT;
    private static XAction FORWARD;
    private static XAction GO;
    private static BooleanAction HIDDEN_FILES;
    private static XAction LANGUAGE;
    private static XAction LOOK_AND_FEEL;
    private static MoveAction MOVE;
    private static XAction NEW_DIR;
    private static XAction NEW_FILE;
    private static SymlinkAction SYMLINK;
    private static OpenAction OPEN;
    private static XAction OTHER_PANEL_DIR;
    //private static XAction PROCESS_MULTIPLE_FILES;
    private static PropertiesAction PROPERTIES;
    private static XAction QUIT;
    private static XAction REFRESH;
    private static RenameAction RENAME;
    private static XAction RESTORE_DEFAULTS;
    private static XAction ROOTS;
    private static XAction SELECT_ALL;
    private static XAction SELECT;
    private static BooleanAction SHOW_ATTRIBUTES;
    private static XAction SORT;
    private static UnzipAction UNZIP;
    private static XAction UP_DIR;
    private static ViewAction VIEW;
    private static ZipAction ZIP;
    private static Collection<XAction> ALL_ACTIONS;

    private static void init() {
        //we must init all actions beforehand to register them globally
        BACK = new BackAction("menu.go.back", false);
        BACK.setAccelerator(KeyEvent.VK_LEFT, InputEvent.CTRL_DOWN_MASK);

        BOOKMARK = new BookmarkAction();
        BOOKMARK.setAccelerator(KeyEvent.VK_B, InputEvent.CTRL_DOWN_MASK);

        CHANGE_ACTIVE_PANEL = new XAction("menu.work.change.active.pnl", false) {
            @Override
            public void act() {
                XPanels.changeActivePanel();
            }
        };
        CHANGE_ACTIVE_PANEL.setAccelerator(KeyEvent.VK_TAB);

        COMPARE_DIRECTORIES = new CompareDirectoriesAction("menu.do.compare.dirs", false);
        COMPARE_DIRECTORIES.setAccelerator(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK);

        if (CONTEXT_MENU == null) {
            CONTEXT_MENU = new XAction("menu.work.context.menu", false) {
                @Override
                public void act() {
                    getTable().showContextMenu();
                }
            };
            CONTEXT_MENU.setAccelerator(KeyEvent.VK_ENTER, InputEvent.SHIFT_DOWN_MASK);
        }

        COPY = new CopyAction("menu.do.copy", true);
        COPY.setAccelerator(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK);
        COPY.setSecondAccelerator(KeyEvent.VK_F5);

        COPY_DIRECTORY = new CopyDirectoryAction("menu.do.copy.dir", false);
        COPY_DIRECTORY.setAccelerator(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_MASK);

        COPY_PATH = new CopyPathAction("menu.do.copy.path", false);
        COPY_PATH.setAccelerator(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_MASK);

        DELETE = new DeleteAction("menu.do.delete", true);
        DELETE.setAccelerator(KeyEvent.VK_DELETE);
        DELETE.setSecondAccelerator(KeyEvent.VK_F8);

        DIRECTORY_AS_TEXT = new DirectoryAsTextAction("menu.go.dir.as.text", true);
        DIRECTORY_AS_TEXT.setAccelerator(KeyEvent.VK_T, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK);

        DIRECTORY_SIZE = new DirectorySizeAction("menu.do.dir.size", false);
        DIRECTORY_SIZE.setAccelerator(KeyEvent.VK_SPACE);

        DIRECTORY_TREE = new DirectoryTreeAction("menu.go.dir.tree", true);
        DIRECTORY_TREE.setAccelerator(KeyEvent.VK_T, InputEvent.CTRL_DOWN_MASK);

        DO = new DoAction("menu.do", false);
        DO.setAccelerator(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK);

        EDIT = new EditAction("menu.do.edit", true);
        EDIT.setAccelerator(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK);
        EDIT.setSecondAccelerator(KeyEvent.VK_F4);

        EXTENSION_COLORS = new ExtensionColorsAction("menu.config.extension.colors", false);

        EYES_FRIENDLY_MODE = new EyesFriendlyModeAction("menu.config.eyes.friendly.mode", false);

        FIND = new FindAction("menu.do.find", true);
        FIND.setAccelerator(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK);

        FONT = new FontAction("menu.config.font", true);

        FORWARD = new ForwardAction("menu.go.forward", false);
        FORWARD.setAccelerator(KeyEvent.VK_RIGHT, InputEvent.CTRL_DOWN_MASK);

        GO = new GoAction("menu.go", false);
        GO.setAccelerator(KeyEvent.VK_G, InputEvent.CTRL_DOWN_MASK);

        HIDDEN_FILES = new HiddenFilesAction("menu.config.hidden.files", false);

        LANGUAGE = new LanguageAction("menu.config.language", true);

        LOOK_AND_FEEL = new XAction("menu.config.look.and.feel", true) {
            @Override
            public void act() {
                lenacom.filer.config.LookAndFeel.chooseLookAndFeel();
            }
        };

        MOVE = new MoveAction("menu.do.move", true);
        MOVE.setAccelerator(KeyEvent.VK_M, InputEvent.CTRL_DOWN_MASK);
        MOVE.setSecondAccelerator(KeyEvent.VK_F6);

        NEW_DIR = new NewDirectoryAction("menu.do.new.dir", true);
        NEW_DIR.setAccelerator(KeyEvent.VK_INSERT);
        NEW_DIR.setSecondAccelerator(KeyEvent.VK_F7);

        NEW_FILE = new NewFileAction("menu.do.new.file", true);
        NEW_FILE.setAccelerator(KeyEvent.VK_INSERT, InputEvent.SHIFT_DOWN_MASK);
        NEW_FILE.setSecondAccelerator(KeyEvent.VK_F4, InputEvent.ALT_DOWN_MASK);

        //we can't set the accelerator [Enter] here
        OPEN = new OpenAction("menu.do.open", false);
        OPEN.setAccelerator(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK);

        OTHER_PANEL_DIR = new OtherPanelDirectoryAction("menu.go.other.pnl", false);
        OTHER_PANEL_DIR.setAccelerator(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK);

        /*PROCESS_MULTIPLE_FILES = new XAction("Process Multiple Files...", KeyEvent.VK_M) {
            @Override
            public void act() {
                System.out.println("Process Multiple Files");
            }
        };*/

        PROPERTIES = new PropertiesAction("menu.do.properties", true);
        PROPERTIES.setAccelerator(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK);
        PROPERTIES.setSecondAccelerator(KeyEvent.VK_F9);

        QUIT = new QuitAction("menu.work.quit", false);
        QUIT.setAccelerator(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK);
        QUIT.setSecondAccelerator(KeyEvent.VK_F10);

        REFRESH = new RefreshAction("menu.do.refresh", false);
        REFRESH.setAccelerator(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK);

        RENAME = new RenameAction("menu.do.rename", true);
        RENAME.setAccelerator(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK);
        RENAME.setSecondAccelerator(KeyEvent.VK_F2);

        RESTORE_DEFAULTS = new RestoreDefaultsAction("menu.config.restore.defaults", true);

        ROOTS = new RootsAction("menu.go.roots", true);
        ROOTS.setAccelerator(KeyEvent.VK_0, InputEvent.CTRL_DOWN_MASK);

        SELECT_ALL = new XAction("menu.do.select.all", false) {
            @Override
            public void act() {
                getTable().selectAll();
            }
        };
        SELECT_ALL.setAccelerator(KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK);

        SHOW_ATTRIBUTES = new ShowAttributesAction("menu.config.show.attrs", false);

        SELECT = new SelectAction("menu.do.select", true);
        SELECT.setAccelerator(KeyEvent.VK_ADD);
        SELECT.setSecondAccelerator(KeyEvent.VK_SUBTRACT);

        SORT = new SortAction("menu.do.sort", true);
        SORT.setAccelerator(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK);

        SYMLINK = new SymlinkAction("menu.do.symlink", true);
        SYMLINK.setAccelerator(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK);

        UNZIP = new UnzipAction("menu.do.unzip", true);
        UNZIP.setAccelerator(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK);

        UP_DIR = new UpDirectoryAction("menu.go.up.dir", false);
        UP_DIR.setAccelerator(KeyEvent.VK_U, InputEvent.CTRL_DOWN_MASK);

        VIEW = new ViewAction("menu.do.view", true);
        VIEW.setAccelerator(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK);
        VIEW.setSecondAccelerator(KeyEvent.VK_F3);

        ZIP = new ZipAction("menu.do.zip", true);
        ZIP.setAccelerator(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK);

        XAction[] allActions = new XAction[] {
            BACK, BOOKMARK, CHANGE_ACTIVE_PANEL, COMPARE_DIRECTORIES,
            CONTEXT_MENU, COPY, COPY_PATH, DELETE, DIRECTORY_SIZE, DIRECTORY_TREE,
            DO, EDIT, EXTENSION_COLORS, EYES_FRIENDLY_MODE, FIND, FONT, FORWARD,
            GO, HIDDEN_FILES, LANGUAGE, LOOK_AND_FEEL, MOVE, NEW_DIR, NEW_FILE,
            SYMLINK, OPEN, OTHER_PANEL_DIR, DIRECTORY_AS_TEXT, PROPERTIES, QUIT,
            REFRESH, RENAME, RESTORE_DEFAULTS, ROOTS, SELECT_ALL, SELECT, SORT,
            UNZIP, UP_DIR, VIEW, ZIP
        };
        ALL_ACTIONS = Arrays.asList(allActions);
    }

    static {
        init();
    }

    public static void reload() {
        init();
    }

    public static Collection<XAction> getAllActions() {
        return ALL_ACTIONS;
    }

    public static XAction getBackAction() {
        return BACK;
    }

    public static BookmarkAction getBookmarkAction() {
        return BOOKMARK;
    }

    public static XAction getChangeActivePanelAction() {
        return CHANGE_ACTIVE_PANEL;
    }

    public static XAction getCompareDirectoriesAction() {
        return COMPARE_DIRECTORIES;
    }

    public static XAction getContextMenuAction() {
        return CONTEXT_MENU;
    }

    public static CopyAction getCopyAction() {
        return COPY;
    }

    public static XAction getCopyDirectoryAction() {
        return COPY_DIRECTORY;
    }

    public static CopyPathAction getCopyPathAction() {
        return COPY_PATH;
    }

    public static DeleteAction getDeleteAction() {
        return DELETE;
    }

    public static XAction getDirectoryAsTextAction() {
        return DIRECTORY_AS_TEXT;
    }

    public static DirectorySizeAction getDirectorySizeAction() {
        return DIRECTORY_SIZE;
    }

    public static XAction getDirectoryTreeAction() {
        return DIRECTORY_TREE;
    }

    public static XAction getDoAction() {
        return DO;
    }

    public static EditAction getEditAction() {
        return EDIT;
    }

    public static BooleanAction getExtensionColorsAction() {
        return EXTENSION_COLORS;
    }

    public static BooleanAction getEyesFriendlyModeAction() {
        return EYES_FRIENDLY_MODE;
    }

    public static XAction getFindAction() {
        return FIND;
    }

    public static XAction getFontAction() {
        return FONT;
    }

    public static XAction getForwardAction() {
        return FORWARD;
    }

    public static XAction getGoAction() {
        return GO;
    }

    public static BooleanAction getHiddenFilesAction() {
        return HIDDEN_FILES;
    }

    public static XAction getLanguageAction() {
        return LANGUAGE;
    }

    public static XAction getLookAndFeelAction() {
        return LOOK_AND_FEEL;
    }

    public static MoveAction getMoveAction() {
        return MOVE;
    }

    public static XAction getNewDirAction() {
        return NEW_DIR;
    }

    public static XAction getNewFileAction() {
        return NEW_FILE;
    }

    public static OpenAction getOpenAction() {
        return OPEN;
    }

    public static XAction getOtherPanelDirAction() {
        return OTHER_PANEL_DIR;
    }

    /*public static XAction getProcessMultipleFilesAction() {
        return PROCESS_MULTIPLE_FILES;
    }*/

    public static PropertiesAction getPropertiesAction() {
        return PROPERTIES;
    }

    public static XAction getQuitAction() {
        return QUIT;
    }

    public static XAction getRefreshAction() {
        return REFRESH;
    }

    public static RenameAction getRenameAction() {
        return RENAME;
    }

    public static XAction getRestoreDefaultsAction() {
        return RESTORE_DEFAULTS;
    }

    public static XAction getRootsAction() {
        return ROOTS;
    }

    public static XAction getSelectAllAction() {
        return SELECT_ALL;
    }

    public static XAction getSelectAction() {
        return SELECT;
    }

    public static BooleanAction getShowAttributesAction() {
        return SHOW_ATTRIBUTES;
    }

    public static XAction getSortAction() {
        return SORT;
    }

    public static SymlinkAction getSymlinkAction() {
        return SYMLINK;
    }

    public static UnzipAction getUnzipAction() {
        return UNZIP;
    }

    public static XAction getUpDirAction() {
        return UP_DIR;
    }

    public static ViewAction getViewAction() {
        return VIEW;
    }

    public static ZipAction getZipAction() {
        return ZIP;
    }
}

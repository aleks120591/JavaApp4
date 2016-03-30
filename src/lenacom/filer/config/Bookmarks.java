package lenacom.filer.config;

import lenacom.filer.path.PathUtils;
import lenacom.filer.zip.ZipWorker;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum Bookmarks {
    INSTANCE;

    public static interface BookmarksListener {
        void bookmarksChanged(List<Bookmark> bookmarks);
    }

    private List<Bookmark> bookmarks;
    private List<BookmarksListener> listeners;

    public static void addListener(BookmarksListener listener) {
        INSTANCE.listeners.add(listener);
    }

    public static void removeListener(BookmarksListener listener) {
        INSTANCE.listeners.remove(listener);
    }

    private Bookmarks() {
        bookmarks = new ArrayList<>();
        listeners = new ArrayList<>();
        
        String data = Configuration.getString(Configuration.BOOKMARKS);
        if (data != null) {
            String[] rows = data.split("\n");
            for (String row : rows) {
                String[] parts = row.split("\t");
                if (parts.length == 2) {
                    String name = parts[0];
                    Path path = Paths.get(parts[1]);
                    if (pathExists(path)) {
                        bookmarks.add(new Bookmark(name, path));
                    }
                }
            }
        }
        Collections.sort(bookmarks);
        
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                StringBuilder sb = new StringBuilder();
                for (Bookmark bookmark : bookmarks) {
                    if (sb.length() > 0) sb.append("\n");
                    sb.append(bookmark.getName())
                      .append("\t")
                      .append(bookmark.getPath());
                }
                Configuration.setString(Configuration.BOOKMARKS, sb.toString());
            }
        });
    }

    private static boolean pathExists(Path path) {
        if (PathUtils.existsFollowLink(path)) return true;
        Path parent = PathUtils.getClosestExistentParent(path);
        if (PathUtils.isZip(parent)) {
            try {
                return ZipWorker.createAndRead(parent).getPathProcessor().directoryExists(path);
            } catch (IOException e) {
                return false;
            }
        }
        return false;
    }

    public static Bookmark getBookmarkByPath(Path path) {
        assert(SwingUtilities.isEventDispatchThread());
        if (path == null) return null;

        for (Bookmarks.Bookmark bookmark : Bookmarks.getBookmarks()) {
            if (bookmark.getPath().equals(path)) {
                return bookmark;
            }
        }
        return null;
    }

    public static void addBookmark(String name, Path path) {
        assert(SwingUtilities.isEventDispatchThread());
        INSTANCE.bookmarks.add(new Bookmark(name, path));
        Collections.sort(INSTANCE.bookmarks);
        INSTANCE.notifyListeners();
    }

    public static void deleteBookmark(Bookmark bookmark) {
        assert(SwingUtilities.isEventDispatchThread());
        INSTANCE.bookmarks.remove(bookmark);
        INSTANCE.notifyListeners();
    }

    public static List<Bookmark> getBookmarks() {
        return Collections.unmodifiableList(INSTANCE.bookmarks);
    }

    private void notifyListeners() {
        for (BookmarksListener listener: INSTANCE.listeners) {
            listener.bookmarksChanged(INSTANCE.bookmarks);
        }
    }

    public static class Bookmark implements Comparable<Bookmark> {
        private String name;
        private final Path path;

        public Bookmark(String name, Path path) {
            this.name = name;
            this.path = path;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Path getPath() {
            return path;
        }
        
        public String toString() {
            return name;
        }

        @Override
        public int compareTo(Bookmark otherBookmark) {
            return this.getName().compareTo(otherBookmark.getName());
        }
    }
}

package lenacom.filer.zip;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

class ZipModel {
    private Path zip;
    private String absolutePrefix;
    private int zipPrefixLength;
    private ConcurrentMap<Path, Set<Path>> parentToChildren ; //keys and values are absolute paths
    private ConcurrentMap<Path, ZipEntry> pathToEntry; //keys are absolute paths
    private Set<ZipEntry> allEntries;
    private ZipPathProcessor processor;
    private ConcurrentMap<Path, Long> directorySize = new ConcurrentHashMap<>();

    ZipModel(Path zip) {
        this.zip = zip;
        absolutePrefix = zip.toString() + File.separator;
        zipPrefixLength = zip.toString().length();
    }

    private void init() {
        parentToChildren = new ConcurrentHashMap<>();
        pathToEntry = new ConcurrentHashMap<>();
        if (allEntries == null) {
            return;
            //zip file hasn't been read
        }
        for (ZipEntry entry: allEntries) {
            Path path = toAbsoluteZipPath(entry.getName());
            pathToEntry.put(path, entry);
            if (entry.isDirectory()) {
                if (parentToChildren.get(path) == null) {
                    parentToChildren.put(path, new HashSet<Path>());
                }
            }

            Path parent = path.getParent();
            while(!parent.equals(zip)) {
                addChild(parent, path);
                path = parent;
                parent = path.getParent();
            }
            addChild(zip, path);
        }
        for (Path key: parentToChildren.keySet()) {
            parentToChildren.put(key, Collections.unmodifiableSet(parentToChildren.get(key)));
        }
    }

    private void addChild(Path parent, Path child) {
        assert(parent.isAbsolute());
        assert(child.isAbsolute());
        Set<Path> children = parentToChildren.get(parent);
        if (children == null) {
            children = new HashSet<>();
        }
        children.add(child);
        parentToChildren.put(parent, children);
    }

    Path getZip() {
        return zip;
    }

    Set<Path> getChildren(Path parent) {
        if (parentToChildren == null) init();
        if (!parent.isAbsolute()) parent = toAbsoluteZipPath(parent);
        return parentToChildren.get(parent);
    }

    ZipEntry getEntry(Path path) {
        if (pathToEntry == null) init();
        if (!path.isAbsolute()) path = toAbsoluteZipPath(path);
        return pathToEntry.get(path);
    }

    void readOnce() throws IOException {
        if (allEntries == null) read();
    }

    private synchronized void read() throws IOException {
        allEntries = new HashSet<>();
        try ( ZipFile zipFile = new ZipFile(zip.toFile()) ) {
            Enumeration en = zipFile.entries();
            while(en.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) en.nextElement();
                allEntries.add(entry);
            }
        }
        allEntries = Collections.unmodifiableSet(allEntries);
    }

    ZipPathProcessor getProcessor() {
        if (processor == null) processor = new ZipPathProcessor(this);
        return processor;
    }

    private Path toAbsoluteZipPath(String path) {
        return Paths.get(absolutePrefix + path);
    }

    private Path toAbsoluteZipPath(Path path) {
        if (path.isAbsolute()) return path;
        return toAbsoluteZipPath(path.toString());
    }

    Path toRelativeZipPath(Path path) {
        if (!path.isAbsolute() || !path.startsWith(zip)) return path;
        if (path.equals(zip)) return null;
        return Paths.get(path.toString().substring(zipPrefixLength + 1));
    }

    Path[] toRelativeZipPath(Path[] paths) {
        List<Path> result = new ArrayList<>();
        for (Path path: paths) {
            result.add(toRelativeZipPath(path));
        }
        return result.toArray(new Path[result.size()]);
    }

    Long getDirectorySize(Path path) {
        if (!path.isAbsolute()) path = toAbsoluteZipPath(path);
        return directorySize.get(path);
    }

    void setDirectorySize(Path path, long size) {
        if (!path.isAbsolute()) path = toAbsoluteZipPath(path);
        directorySize.put(path, size);
    }
}

package lenacom.filer.zip;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

class ZipLocker {
    private static Set<Path> locked = new HashSet<>();

    synchronized static boolean lock(Path zip) {
        if (locked.contains(zip)) return false;
        locked.add(zip);
        return true;
    }

    synchronized static void unlock(Path zip) {
        locked.remove(zip);
    }
}

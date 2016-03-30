package lenacom.filer.path.processor;

import java.nio.file.Path;
import java.util.Date;

public interface PathProcessor {
    Long getSize(Path path) throws Exception;
    Date getLastModified(Path path) throws Exception;
    boolean fileExists(Path path);
    boolean directoryExists(Path path);
    boolean isDirectory(Path path) throws Exception;
    boolean isReadonly(Path path);
}

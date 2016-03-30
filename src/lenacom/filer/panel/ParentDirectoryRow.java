package lenacom.filer.panel;

import lenacom.filer.path.NativePathWithAttributes;
import lenacom.filer.zip.ZipPathProcessor;

import java.io.IOException;
import java.nio.file.Path;

public class ParentDirectoryRow extends DirectoryRow {
    private static final String PARENT_DIR_NAME = "..";
    private String html;

    ParentDirectoryRow(NativePathWithAttributes pwa) throws IOException {
        super(pwa);
        this.name = PARENT_DIR_NAME;
    }

    ParentDirectoryRow(Path path, ZipPathProcessor pp) {
        super(path, pp);
        this.name = PARENT_DIR_NAME;
    }

    @Override
    public boolean canCalculateSize() {
        return false;
    }

    @Override
    public String toHtml() {
        if (html == null) {
            html = path.toAbsolutePath().toString();
        }
        return html;
    }
}

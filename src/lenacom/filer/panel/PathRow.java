package lenacom.filer.panel;

import lenacom.filer.path.PathSize;

import javax.swing.*;
import java.nio.file.Path;
import java.util.Date;

public interface PathRow {
    Path getPath();
    boolean isSymlink();
    Path getSymlinkTarget();
    String getName();
    String getExtension();
    PathSize getSize();
    Date getDate();
    String getFormattedDate();
    String toHtml();
    String getAttributes();
    Icon getIcon();
    boolean isSizeApproximate();
}

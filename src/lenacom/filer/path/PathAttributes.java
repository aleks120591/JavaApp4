package lenacom.filer.path;

import lenacom.filer.message.Errors;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.DosFileAttributes;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.util.HashSet;
import java.util.Set;

public class PathAttributes {
    private volatile static boolean isDosAttributesSupported = true; //true by default
    private volatile static boolean isPosixAttributesSupported = true; //true by default
    private volatile static boolean isUserDefinedAttributesSupported = true; //true by default

    private static final String DIRECTORY_SIZE = "dir.size";
    private static final String FILE_CHARSET = "file.charset";
    private static final String CHARSET = "UTF-8";

    public static DosFileAttributes getDosAttributes(Path path) {
        if (!isDosAttributesSupported) return null;
        try {
            return Files.readAttributes(path, DosFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
        } catch (UnsupportedOperationException e) {
            isDosAttributesSupported = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void setAttribute(Path path, String attribute, Object value) throws IOException {
        Files.setAttribute(path, attribute, value, LinkOption.NOFOLLOW_LINKS);
    }

    public static Set<PosixFilePermission> getPosixAttributes(Path path) {
        if (!isPosixAttributesSupported) return null;
        try {
            return Files.getPosixFilePermissions(path, LinkOption.NOFOLLOW_LINKS);
        } catch (UnsupportedOperationException e) {
            isPosixAttributesSupported = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void setPosixAttributes(Path path, Set<PosixFilePermission> attributes) {
        if (!isPosixAttributesSupported) return;
        try {
            Files.setPosixFilePermissions(path, attributes);
        } catch (UnsupportedOperationException e) {
            isPosixAttributesSupported = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isReadonly(Path path) {
        if (!isDosAttributesSupported) return false;
        try {
            return (Boolean) Files.getAttribute(path, "dos:readonly", LinkOption.NOFOLLOW_LINKS);
        } catch (UnsupportedOperationException e) {
            isDosAttributesSupported = false;
        } catch (IOException e) {
            Errors.showError(e);
        }

        return false;
    }

    public static void setReadonly(Path path, boolean readonly) {
        if (!isDosAttributesSupported) return;
        try {
            Files.setAttribute(path, "dos:readonly", readonly, LinkOption.NOFOLLOW_LINKS);
        } catch (UnsupportedOperationException e) {
            isDosAttributesSupported = false;
        } catch (IOException e) {
            Errors.showError(e);
        }
    }

    public static Long getDirectorySize(Path dir) {
        if (!isUserDefinedAttributesSupported) return null;
        try {
            UserDefinedFileAttributeView view = Files.getFileAttributeView(dir, UserDefinedFileAttributeView.class);
            ByteBuffer buf = ByteBuffer.allocate(view.size(DIRECTORY_SIZE));
            view.read(DIRECTORY_SIZE, buf);
            buf.flip();
            return buf.getLong();
        } catch (UnsupportedOperationException e) {
            isUserDefinedAttributesSupported = false;
        } catch (Exception e) {
            //do nothing
        }
        return null;
    }

    //will cause a path modified event
    public static boolean setDirectorySize(Path dir, Long size) {
        if (!isUserDefinedAttributesSupported) return false;
        try {
            Long oldSize = getDirectorySize(dir);
            if (size.equals(oldSize)) return false; //avoid updating a file, avoid extra path modified events
            UserDefinedFileAttributeView view = Files.getFileAttributeView(dir, UserDefinedFileAttributeView.class);
            ByteBuffer buf = ByteBuffer.allocate(Long.SIZE).putLong(size);
            buf.flip();
            view.write(DIRECTORY_SIZE, buf);
            return true;
        } catch (UnsupportedOperationException e) {
            isUserDefinedAttributesSupported = false;
        } catch (Exception e) {
            //do nothing
        }
        return false;
    }

    public static Charset getFileCharset(Path file) {
        if (!isUserDefinedAttributesSupported) return null;
        try {
            UserDefinedFileAttributeView view = Files.getFileAttributeView(file, UserDefinedFileAttributeView.class);
            ByteBuffer buf = ByteBuffer.allocate(view.size(FILE_CHARSET));
            view.read(FILE_CHARSET, buf);
            buf.flip();
            String charsetName = new String(buf.array(), CHARSET); 
            return Charset.forName(charsetName);
        } catch (UnsupportedOperationException e) {
            isUserDefinedAttributesSupported = false;
        } catch (Exception e) {
            //do nothing
        }
        return null;
    }

    //will cause a path modified event
    public static boolean setFileCharset(Path file, Charset charset) {
        if (!isUserDefinedAttributesSupported) return false;
        try {
            Charset oldCharset = getFileCharset(file);
            if (charset.equals(oldCharset)) return false; //avoid updating a file, avoid extra path modified events
            UserDefinedFileAttributeView view = Files.getFileAttributeView(file, UserDefinedFileAttributeView.class);
            String charsetName = charset.name();
            byte[] bytes = charsetName.getBytes(CHARSET);
            ByteBuffer buf = ByteBuffer.allocate(bytes.length).put(bytes);
            buf.flip();
            view.write(FILE_CHARSET, buf);
            return true;
        } catch (UnsupportedOperationException e) {
            isUserDefinedAttributesSupported = false;
        } catch (Exception e) {
            //do nothing
        }
        return false;
    }
}


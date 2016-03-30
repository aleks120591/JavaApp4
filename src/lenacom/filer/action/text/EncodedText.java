package lenacom.filer.action.text;

import lenacom.filer.config.Charsets;
import lenacom.filer.config.Configuration;
import lenacom.filer.message.Errors;
import lenacom.filer.message.Messages;
import lenacom.filer.path.PathAttributes;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.*;
import java.nio.file.Files;
import java.nio.file.Path;

class EncodedText {
    private final static Charset[] STANDARD_CHARSETS = new Charset[] {
        StandardCharsets.UTF_8,
        StandardCharsets.US_ASCII,
        StandardCharsets.ISO_8859_1,
        StandardCharsets.UTF_16,
        StandardCharsets.UTF_16BE,
        StandardCharsets.UTF_16LE
    };

    private Path file;
    private Charset currentCharset;
    private byte[] bytes;
    private String text;

    EncodedText(Path file) throws IOException {
        this.file = file;
        this.bytes = Files.readAllBytes(file);

        Charset charset;
        if (bytes.length == 0) {
            charset = StandardCharsets.UTF_8; //new file
        } else {
            charset = PathAttributes.getFileCharset(file);
            if (charset == null) charset = Charsets.getDefaultCharset();
        }

        text = tryToDecode(charset);
        if (text == null) text = decodeWithFirstSuitableCharset();
    }

    private String tryToDecode(Charset charset) {
        CharsetDecoder decoder = charset.newDecoder();
        CharBuffer charBuffer = null;
        try {
            charBuffer = decoder.decode(ByteBuffer.wrap(bytes));
        } catch (CharacterCodingException e) {
            //it's OK
        }
        if (charBuffer != null) {
            String text = charBuffer.toString();
            currentCharset = charset;
            Configuration.setString(Configuration.DEFAULT_CHARSET, currentCharset.name());
            PathAttributes.setFileCharset(file, currentCharset);
            return text;
        } else {
            return null;
        }
    }

    private String decodeWithFirstSuitableCharset() {
        String text;
        if (currentCharset != null) {
            text = tryToDecode(currentCharset);
            if (text != null) return text;
        }

        for (Charset charset : STANDARD_CHARSETS) {
            text = tryToDecode(charset);
            if (text != null) return text;
        }

        outer:
        for (Charset charset : Charset.availableCharsets().values()) {
            for (Charset standardCharset: STANDARD_CHARSETS) {
                if (standardCharset.equals(charset)) {
                    //we have already tried standard charsets
                    continue outer;
                }
            }
            text = tryToDecode(charset);
            if (text != null) return text;
        }
        Messages.showMessage("err.failed.decode.file", file);
        return "";
    }

    void setCurrentCharset(Charset newCharset) {
        if (currentCharset.name().equalsIgnoreCase(newCharset.name())) return;
        String text = tryToDecode(newCharset);
        if (text != null) this.text = text;
    }

    String getText() {
        return text;
    }

    void setText(String text) {
        CharsetEncoder encoder = currentCharset.newEncoder();
        try {
            ByteBuffer buf = encoder.encode(CharBuffer.wrap(text));
            bytes = new byte[buf.remaining()];
            buf.get(bytes, 0, bytes.length);
        } catch (CharacterCodingException e) {
            Errors.showError(e);
        }
        this.text = text;
    }

    byte[] getBytes() {
        return bytes;
    }

    Charset getCurrentCharset() {
        return currentCharset;
    }
}

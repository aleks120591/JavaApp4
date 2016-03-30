package lenacom.filer.action.find;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;

class EncodedNeedle {
    private byte[][] encodedNeedle;
    private Charset charset;
    private int maxMatchedBytes = -1;
    private boolean[] countMatchedBytes;
    private int lengthInBytes;
    private CharsetDecoder decoder;
    private int marginLengthInBytes = -1;

    EncodedNeedle(String needle, Charset charset, boolean caseSensitive) throws Exception {
        CharsetEncoder encoder = charset.newEncoder();
        encodedNeedle = caseSensitive?
            new byte[][] { encodeNeedle(encoder, needle) } :
            new byte[][] { encodeNeedle(encoder, needle.toLowerCase() /*lower case is more probable*/),
                    encodeNeedle(encoder, needle.toUpperCase())};
        if (encodedNeedle.length == 2) assert(encodedNeedle[0].length == encodedNeedle[1].length);
        lengthInBytes = encodedNeedle[0].length;
        this.charset = charset;
        countMatchedBytes = new boolean[lengthInBytes];
    }

    private byte[] encodeNeedle(CharsetEncoder encoder, String needle) throws Exception {
        ByteBuffer buf = encoder.encode(CharBuffer.wrap(needle));
        byte[] encodedNeedle = new byte[buf.remaining()];
        buf.get(encodedNeedle, 0, encodedNeedle.length);
        return encodedNeedle;
    }

    Charset getCharset() {
        return charset;
    }

    int getLengthInBytes() {
        return lengthInBytes;
    }

    void setMarginLengthInBytes(int marginLengthInBytes) {
        this.marginLengthInBytes = marginLengthInBytes;
    }

    int getMarginLengthInBytes() {
        return marginLengthInBytes;
    }

    void clear() {
        maxMatchedBytes = -1;
        for (int i = 0; i < lengthInBytes; i++) {
            countMatchedBytes[i] = false;
        }
    }

    //return true if all bytes match (needle found)
    boolean found(byte b) {
        int newMaxMatchedBytes = -1;
        for (int i = maxMatchedBytes + 1; i >= 0; i--) {
            if ((i == 0 || countMatchedBytes[i - 1]) && match(b, i)) {
                countMatchedBytes[i] = true;
                if (newMaxMatchedBytes == -1 && (i < lengthInBytes - 1)) {
                    newMaxMatchedBytes = i;
                }
            }
            if (i > 0) countMatchedBytes[i - 1] = false;
        }
        maxMatchedBytes = newMaxMatchedBytes;

        boolean found = countMatchedBytes[lengthInBytes - 1];
        countMatchedBytes[lengthInBytes - 1] = false;
        return found;
    }

    private boolean match(byte b, int index) {
        return b == encodedNeedle[0][index] ||
                encodedNeedle.length == 2 && b == encodedNeedle[1][index];
    }

    CharsetDecoder getDecoder() {
        if (decoder == null) {
            decoder = charset.newDecoder();
            decoder.onMalformedInput(CodingErrorAction.IGNORE);
        }
        return decoder;
    }
}

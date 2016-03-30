package lenacom.filer.action.find;

import lenacom.filer.config.Charsets;
import lenacom.filer.path.PathAttributes;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

class Finder {
    private final int BUFFER_SIZE = 1024;
    private final int EXTRACT_MARGIN_IN_CHARS = BUFFER_SIZE / 16;
    private final int EXTRACTS_LIMIT = 100;

    private final String contains;
    private final boolean caseSensitive;
    private final boolean allExtracts;
    private ConcurrentHashMap<String, EncodedNeedle> charsetToNeedle;
    private int needleLengthInChars;

    private List<Charset> defaultCharsets = Charsets.getCharsets();

    Finder(String contains, boolean caseSensitive, boolean allExtracts) {
        assert(contains.length() < BUFFER_SIZE);
        this.contains = contains;
        this.caseSensitive = caseSensitive;
        this.allExtracts = allExtracts;
        this.needleLengthInChars = contains.length();
        charsetToNeedle = new ConcurrentHashMap<>();

        defaultCharsets = Charsets.getCharsets();
    }

    List<ExtractDetails> find(Path path) {
        return new Processor(path).find();
    }

    private class Buffer {
        private byte[] buffer;
        private int count;

        private Buffer(int size) {
            buffer = new byte[size];
        }

        void readFrom(InputStream in) throws IOException {
            if (in.available() > 0) {
                count = in.read(buffer);
            }
        }

        private byte[] getBytes() {
            return buffer;
        }

        private byte getByte(int i) {
            return buffer[i];
        }

        private int getCount() {
            return count;
        }
    }

    private class Processor {
        private Path path;
        private List<EncodedNeedle> encodedNeedles;
        //we need 3 buffers to get extracts
        private Buffer buffer, prevBuffer, nextBuffer;

        Processor(Path path) {
            this.path = path;
            encodedNeedles = getEncodedNeedles(path);
            if (encodedNeedles.size() == 0) throw new NoNeedleException(contains, path);
            buffer = new Buffer(BUFFER_SIZE);
        }

        private List<EncodedNeedle> getEncodedNeedles(Path path) {
            Charset charset = PathAttributes.getFileCharset(path);
            List<EncodedNeedle> encodedNeedles = Collections.EMPTY_LIST;
            if (charset != null) {
                List<Charset> charsets = new ArrayList<>();
                charsets.add(charset);
                encodedNeedles = getEncodedNeedles(charsets);
            }
            if (encodedNeedles.size() == 0) {
                encodedNeedles = getEncodedNeedles(defaultCharsets);
            }
            return encodedNeedles;
        }

        private List<EncodedNeedle> getEncodedNeedles(List<Charset> charsets) {
            List<EncodedNeedle> encodedNeedles = new ArrayList<>();
            for (Charset charset: charsets) {
                EncodedNeedle charsetEncodedNeedle = charsetToNeedle.get(charset.name());
                if (charsetEncodedNeedle == null) {
                    try {
                        charsetEncodedNeedle = new EncodedNeedle(contains, charset, caseSensitive);
                        charsetToNeedle.put(charset.name(), charsetEncodedNeedle);
                    } catch (Exception e) {
                        //do nothing
                    }
                } else {
                    charsetEncodedNeedle.clear();
                }
                if (charsetEncodedNeedle != null) encodedNeedles.add(charsetEncodedNeedle);
            }
            return encodedNeedles;
        }

        List<ExtractDetails> find() {
            List<ExtractDetails> extracts = new ArrayList<>();
            try (InputStream in = Files.newInputStream(path)) {
                all:
                while (in.available() > 0 || nextBuffer != null) {
                    if (nextBuffer != null) {
                        buffer = nextBuffer;
                        nextBuffer = null;
                    } else {
                        buffer.readFrom(in);
                    }
                    for (int i = 0, n = buffer.getCount(); i < n; i++) {
                        byte b = buffer.getByte(i);
                        EncodedNeedle foundNeedle = null;
                        for (EncodedNeedle needle: encodedNeedles) {
                            if (needle.found(b)) {
                                if (foundNeedle == null || foundNeedle.getLengthInBytes() != needle.getLengthInBytes()) {
                                    extracts.add(getExtract(in, i, needle));
                                }
                                if (!allExtracts || extracts.size() == EXTRACTS_LIMIT) {
                                    break all;
                                }
                                if (foundNeedle == null) foundNeedle = needle;
                            }
                        }
                    }
                    switchBuffers();
                }
            } catch (IOException e) {
                System.err.println(e);
            }
            return extracts;
        }

        private void switchBuffers() {
            if (prevBuffer == null) {
                prevBuffer = new Buffer(BUFFER_SIZE);
            }
            Buffer tmp = buffer;
            buffer = prevBuffer;
            prevBuffer = tmp;
        }

        private ExtractDetails getExtract(InputStream in, int needleLastByteIndex, EncodedNeedle needle) throws IOException {
            int index = needleLastByteIndex + 1;
            if (needle.getMarginLengthInBytes() < 0) {
                int marginLengthInBytes = EXTRACT_MARGIN_IN_CHARS * (needle.getLengthInBytes() / needleLengthInChars);
                needle.setMarginLengthInBytes(marginLengthInBytes);
            }

            String leftMarginWithNeedle = getLeftMarginWithNeedle(index, needle);
            String rightMargin = getRightMargin(in, index, needle);
            int end = leftMarginWithNeedle.length();
            int start = Math.max(0, end - needleLengthInChars); //if it fails to decode we can receive empty leftMarginWithNeedle
            return new ExtractDetails(leftMarginWithNeedle + rightMargin, needle.getCharset(), start, end);
        }

        private String decode(byte[] buffer, int offset, int length, CharsetDecoder decoder) {
            try {
                CharBuffer charBuffer = decoder.decode(ByteBuffer.wrap(buffer, offset, length));
                if (charBuffer != null) return charBuffer.toString();
            } catch (CharacterCodingException e) {
                e.printStackTrace();
            }
            return "";
        }

        private String getLeftMarginWithNeedle(int index, EncodedNeedle needle) {
            int leftMarginWithNeedleLengthInBytes = needle.getMarginLengthInBytes() + needle.getLengthInBytes();
            //availableLeftBytes = index
            String prevChunk = null;
            if (index < leftMarginWithNeedleLengthInBytes && prevBuffer != null) {
                int prevChunkLengthInBytes = leftMarginWithNeedleLengthInBytes - index;
                prevChunk = decode(prevBuffer.getBytes(),
                    prevBuffer.getCount() - prevChunkLengthInBytes, prevChunkLengthInBytes,
                    needle.getDecoder());
            }
            int chunkLengthInBytes = Math.min(leftMarginWithNeedleLengthInBytes, index);
            String chunk = decode(buffer.getBytes(),
                    index - chunkLengthInBytes, chunkLengthInBytes,
                    needle.getDecoder() );
            return prevChunk == null? chunk : prevChunk + chunk;
        }

        private String getRightMargin(InputStream in, int index, EncodedNeedle needle) throws IOException {
            int availableRightBytes = buffer.getCount() - index;
            String chunk = decode(buffer.getBytes(),
                    index, Math.min(needle.getMarginLengthInBytes(), availableRightBytes),
                    needle.getDecoder());
            String nextChunk = null;
            if (availableRightBytes < needle.getMarginLengthInBytes() && (nextBuffer != null || in.available() > 0)) {
                int nextChunkLengthInBytes = needle.getMarginLengthInBytes() - availableRightBytes;
                if (nextBuffer == null) {
                    nextBuffer = new Buffer(allExtracts? BUFFER_SIZE : Math.min(nextChunkLengthInBytes, in.available()));
                    nextBuffer.readFrom(in);
                }
                nextChunk = decode(nextBuffer.getBytes(),
                    0, Math.min(nextChunkLengthInBytes, nextBuffer.getCount()),
                    needle.getDecoder());
            }
            return nextChunk == null? chunk : chunk + nextChunk;
        }
    }

    static class NoNeedleException extends RuntimeException {
        private String contains;
        private Path path;

        NoNeedleException(String contains, Path path) {
            this.contains = contains;
            this.path = path;
        }

        String getContains() {
            return contains;
        }

        Path getPath() {
            return path;
        }
    }
}

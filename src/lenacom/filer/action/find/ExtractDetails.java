package lenacom.filer.action.find;

import java.nio.charset.Charset;

class ExtractDetails {
    private String extract;
    private Charset charset;
    private int start;
    private int end;

    ExtractDetails(String extract, Charset charset, int start, int end) {
        this.extract = extract;
        this.charset = charset;
        this.start = start;
        this.end = end;
    }

    String getExtract() {
        return extract;
    }

    Charset getCharset() {
        return charset;
    }

    int getStart() {
        return start;
    }

    int getEnd() {
        return end;
    }
}

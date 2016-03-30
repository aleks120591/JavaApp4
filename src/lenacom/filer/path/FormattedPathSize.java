package lenacom.filer.path;

import java.text.NumberFormat;

public class FormattedPathSize implements PathSize {
    private static NumberFormat oneFractionalDigit = NumberFormat.getInstance();
    private static NumberFormat twoFractionalDigits = NumberFormat.getInstance();
    static {
        oneFractionalDigit.setMaximumFractionDigits(1);
        twoFractionalDigits.setMaximumFractionDigits(2);
    }
    private long bytes;
    private double value;
    private PathSizeUnit sizeUnit;

    private String formattedSize;
    private String string;
    private String stringWithBytes;

    public FormattedPathSize(long bytes) {
        this.bytes = bytes;
    }

    @Override
    public String toString() {
        if (string == null) {
            if (formattedSize == null) {
                if (sizeUnit == null) initValue();
                formattedSize = value < 10? twoFractionalDigits.format(value) :
                        value < 100? oneFractionalDigit.format(value) : String.valueOf(Math.round(value));
            }
            string = formattedSize + " " + sizeUnit.toString();
        }
        return string;
    }

    public String toStringWithBytes() {
        if (stringWithBytes == null) {
            stringWithBytes = toString();
            if (bytes >= PathSizeUnit.KB.getBytes()) stringWithBytes += " (" + NumberFormat.getInstance().format(bytes) + " " + PathSizeUnit.B.toString() +")";
        }
        return stringWithBytes;
    }

    @Override
    public long getBytes() {
        return bytes;
    }

    public PathSizeUnit getSizeUnit() {
        if (sizeUnit == null) initValue();
        return sizeUnit;
    }

    public double getValue() {
        if (sizeUnit == null) initValue();
        return value;
    }

    private void initValue() {
        if (bytes < PathSizeUnit.KB.getBytes()) sizeUnit = PathSizeUnit.B;
        else if (bytes < PathSizeUnit.MB.getBytes()) sizeUnit = PathSizeUnit.KB;
        else if (bytes < PathSizeUnit.GB.getBytes()) sizeUnit = PathSizeUnit.MB;
        else sizeUnit = PathSizeUnit.GB;
        value = (double) bytes / sizeUnit.getBytes();
    }
}

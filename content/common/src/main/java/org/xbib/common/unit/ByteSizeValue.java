package org.xbib.common.unit;

import org.xbib.io.stream.StreamInput;
import org.xbib.io.stream.StreamOutput;
import org.xbib.io.stream.Streamable;

import java.io.IOException;
import java.util.Locale;

public class ByteSizeValue implements Streamable {

    private long size;

    private ByteSizeUnit sizeUnit;

    private ByteSizeValue() {
    }

    public ByteSizeValue(long size, ByteSizeUnit sizeUnit) {
        this.size = size;
        this.sizeUnit = sizeUnit;
    }

    public int bytesAsInt() throws IllegalArgumentException {
        long bytes = bytes();
        if (bytes > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("size [" + toString() + "] is bigger than max int");
        }
        return (int) bytes;
    }

    public long bytes() {
        return sizeUnit.toBytes(size);
    }

    public long getBytes() {
        return bytes();
    }

    public long kb() {
        return sizeUnit.toKB(size);
    }

    public long getKb() {
        return kb();
    }

    public long mb() {
        return sizeUnit.toMB(size);
    }

    public long getMb() {
        return mb();
    }

    public long gb() {
        return sizeUnit.toGB(size);
    }

    public long getGb() {
        return gb();
    }

    public long tb() {
        return sizeUnit.toTB(size);
    }

    public long getTb() {
        return tb();
    }

    public long pb() {
        return sizeUnit.toPB(size);
    }

    public long getPb() {
        return pb();
    }

    public double kbFrac() {
        return ((double) bytes()) / ByteSizeUnit.C1;
    }

    public double getKbFrac() {
        return kbFrac();
    }

    public double mbFrac() {
        return ((double) bytes()) / ByteSizeUnit.C2;
    }

    public double getMbFrac() {
        return mbFrac();
    }

    public double gbFrac() {
        return ((double) bytes()) / ByteSizeUnit.C3;
    }

    public double getGbFrac() {
        return gbFrac();
    }

    public double tbFrac() {
        return ((double) bytes()) / ByteSizeUnit.C4;
    }

    public double getTbFrac() {
        return tbFrac();
    }

    public double pbFrac() {
        return ((double) bytes()) / ByteSizeUnit.C5;
    }

    public double getPbFrac() {
        return pbFrac();
    }

    @Override
    public String toString() {
        long bytes = bytes();
        double value = bytes;
        String suffix = "b";
        if (bytes >= ByteSizeUnit.C5) {
            value = pbFrac();
            suffix = "pb";
        } else if (bytes >= ByteSizeUnit.C4) {
            value = tbFrac();
            suffix = "tb";
        } else if (bytes >= ByteSizeUnit.C3) {
            value = gbFrac();
            suffix = "gb";
        } else if (bytes >= ByteSizeUnit.C2) {
            value = mbFrac();
            suffix = "mb";
        } else if (bytes >= ByteSizeUnit.C1) {
            value = kbFrac();
            suffix = "kb";
        }
        return format1Decimals(value, suffix);
    }

    /**
     * Format the double value with a single decimal points, trimming trailing '.0'.
     */
    public static String format1Decimals(double value, String suffix) {
        String p = String.valueOf(value);
        int ix = p.indexOf('.') + 1;
        int ex = p.indexOf('E');
        char fraction = p.charAt(ix);
        if (fraction == '0') {
            if (ex != -1) {
                return p.substring(0, ix - 1) + p.substring(ex) + suffix;
            } else {
                return p.substring(0, ix - 1) + suffix;
            }
        } else {
            if (ex != -1) {
                return p.substring(0, ix) + fraction + p.substring(ex) + suffix;
            } else {
                return p.substring(0, ix) + fraction + suffix;
            }
        }
    }

    public static ByteSizeValue parseBytesSizeValue(String sValue) {
        return parseBytesSizeValue(sValue, null);
    }

    public static ByteSizeValue parseBytesSizeValue(String sValue, ByteSizeValue defaultValue) {
        if (sValue == null) {
            return defaultValue;
        }
        long bytes;
        try {
            String lastTwoChars = sValue.substring(sValue.length() - Math.min(2, sValue.length())).toLowerCase(Locale.ROOT);
            if (lastTwoChars.endsWith("k")) {
                bytes = (long) (Double.parseDouble(sValue.substring(0, sValue.length() - 1)) * ByteSizeUnit.C1);
            } else if (lastTwoChars.endsWith("kb")) {
                bytes = (long) (Double.parseDouble(sValue.substring(0, sValue.length() - 2)) * ByteSizeUnit.C1);
            } else if (lastTwoChars.endsWith("m")) {
                bytes = (long) (Double.parseDouble(sValue.substring(0, sValue.length() - 1)) * ByteSizeUnit.C2);
            } else if (lastTwoChars.endsWith("mb")) {
                bytes = (long) (Double.parseDouble(sValue.substring(0, sValue.length() - 2)) * ByteSizeUnit.C2);
            } else if (lastTwoChars.endsWith("g")) {
                bytes = (long) (Double.parseDouble(sValue.substring(0, sValue.length() - 1)) * ByteSizeUnit.C3);
            } else if (lastTwoChars.endsWith("gb")) {
                bytes = (long) (Double.parseDouble(sValue.substring(0, sValue.length() - 2)) * ByteSizeUnit.C3);
            } else if (lastTwoChars.endsWith("t")) {
                bytes = (long) (Double.parseDouble(sValue.substring(0, sValue.length() - 1)) * ByteSizeUnit.C4);
            } else if (lastTwoChars.endsWith("tb")) {
                bytes = (long) (Double.parseDouble(sValue.substring(0, sValue.length() - 2)) * ByteSizeUnit.C4);
            } else if (lastTwoChars.endsWith("p")) {
                bytes = (long) (Double.parseDouble(sValue.substring(0, sValue.length() - 1)) * ByteSizeUnit.C5);
            } else if (lastTwoChars.endsWith("pb")) {
                bytes = (long) (Double.parseDouble(sValue.substring(0, sValue.length() - 2)) * ByteSizeUnit.C5);
            } else if (lastTwoChars.endsWith("b")) {
                bytes = Long.parseLong(sValue.substring(0, sValue.length() - 1));
            } else {
                bytes = Long.parseLong(sValue);
            }
        } catch (NumberFormatException e) {
            return defaultValue;
        }
        return new ByteSizeValue(bytes, ByteSizeUnit.BYTES);
    }

    public static ByteSizeValue readBytesSizeValue(StreamInput in) throws IOException {
        ByteSizeValue sizeValue = new ByteSizeValue();
        sizeValue.readFrom(in);
        return sizeValue;
    }

    @Override
    public void readFrom(StreamInput in) throws IOException {
        size = in.readVLong();
        sizeUnit = ByteSizeUnit.BYTES;
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        out.writeVLong(bytes());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ByteSizeValue sizeValue = (ByteSizeValue) o;
        return size == sizeValue.size && sizeUnit == sizeValue.sizeUnit;
    }

    @Override
    public int hashCode() {
        int result = (int) (size ^ (size >>> 32));
        result = 31 * result + (sizeUnit != null ? sizeUnit.hashCode() : 0);
        return result;
    }
}
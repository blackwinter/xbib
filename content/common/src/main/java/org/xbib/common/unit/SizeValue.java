package org.xbib.common.unit;

import com.google.common.base.Preconditions;
import org.xbib.io.stream.StreamInput;
import org.xbib.io.stream.StreamOutput;
import org.xbib.io.stream.Streamable;

import java.io.IOException;
import java.text.ParseException;

public class SizeValue implements Streamable {

    private long size;

    private SizeUnit sizeUnit;

    private SizeValue() {

    }

    public SizeValue(long singles) {
        this(singles, SizeUnit.SCALAR);
    }

    public SizeValue(long size, SizeUnit sizeUnit) {
        Preconditions.checkArgument(size >= 0, "size in SizeValue may not be negative");
        this.size = size;
        this.sizeUnit = sizeUnit;
    }

    public long scalar() {
        return sizeUnit.toScalar(size);
    }

    public long getScalar() {
        return scalar();
    }

    public long kilo() {
        return sizeUnit.toKilo(size);
    }

    public long getKilo() {
        return kilo();
    }

    public long mega() {
        return sizeUnit.toMega(size);
    }

    public long getMega() {
        return mega();
    }

    public long giga() {
        return sizeUnit.toGiga(size);
    }

    public long getGiga() {
        return giga();
    }

    public long tera() {
        return sizeUnit.toTera(size);
    }

    public long getTera() {
        return tera();
    }

    public long peta() {
        return sizeUnit.toPeta(size);
    }

    public long getPeta() {
        return peta();
    }

    public double kiloFrac() {
        return ((double) scalar()) / SizeUnit.C1;
    }

    public double getKiloFrac() {
        return kiloFrac();
    }

    public double megaFrac() {
        return ((double) scalar()) / SizeUnit.C2;
    }

    public double getMegaFrac() {
        return megaFrac();
    }

    public double gigaFrac() {
        return ((double) scalar()) / SizeUnit.C3;
    }

    public double getGigaFrac() {
        return gigaFrac();
    }

    public double teraFrac() {
        return ((double) scalar()) / SizeUnit.C4;
    }

    public double getTeraFrac() {
        return teraFrac();
    }

    public double petaFrac() {
        return ((double) scalar()) / SizeUnit.C5;
    }

    public double getPetaFrac() {
        return petaFrac();
    }

    @Override
    public String toString() {
        long scalar = scalar();
        double value = scalar;
        String suffix = "";
        if (scalar >= SizeUnit.C5) {
            value = petaFrac();
            suffix = "p";
        } else if (scalar >= SizeUnit.C4) {
            value = teraFrac();
            suffix = "t";
        } else if (scalar >= SizeUnit.C3) {
            value = gigaFrac();
            suffix = "g";
        } else if (scalar >= SizeUnit.C2) {
            value = megaFrac();
            suffix = "m";
        } else if (scalar >= SizeUnit.C1) {
            value = kiloFrac();
            suffix = "k";
        }

        return format1Decimals(value, suffix);
    }

    private static String format1Decimals(double value, String suffix) {
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

    public static SizeValue parseSizeValue(String sValue) throws ParseException {
        return parseSizeValue(sValue, null);
    }

    public static SizeValue parseSizeValue(String sValue, SizeValue defaultValue) throws ParseException {
        if (sValue == null) {
            return defaultValue;
        }
        long singles;
        try {
            if (sValue.endsWith("b")) {
                singles = Long.parseLong(sValue.substring(0, sValue.length() - 1));
            } else if (sValue.endsWith("k") || sValue.endsWith("K")) {
                singles = (long) (Double.parseDouble(sValue.substring(0, sValue.length() - 1)) * SizeUnit.C1);
            } else if (sValue.endsWith("m") || sValue.endsWith("M")) {
                singles = (long) (Double.parseDouble(sValue.substring(0, sValue.length() - 1)) * SizeUnit.C2);
            } else if (sValue.endsWith("g") || sValue.endsWith("G")) {
                singles = (long) (Double.parseDouble(sValue.substring(0, sValue.length() - 1)) * SizeUnit.C3);
            } else if (sValue.endsWith("t") || sValue.endsWith("T")) {
                singles = (long) (Double.parseDouble(sValue.substring(0, sValue.length() - 1)) * SizeUnit.C4);
            } else if (sValue.endsWith("p") || sValue.endsWith("P")) {
                singles = (long) (Double.parseDouble(sValue.substring(0, sValue.length() - 1)) * SizeUnit.C5);
            } else {
                singles = Long.parseLong(sValue);
            }
        } catch (NumberFormatException e) {
            throw new ParseException("failed to parse " + sValue, 0);
        }
        return new SizeValue(singles, SizeUnit.SCALAR);
    }

    public static SizeValue readSizeValue(StreamInput in) throws IOException {
        SizeValue sizeValue = new SizeValue();
        sizeValue.readFrom(in);
        return sizeValue;
    }

    @Override
    public void readFrom(StreamInput in) throws IOException {
        size = in.readVLong();
        sizeUnit = SizeUnit.SCALAR;
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        out.writeVLong(scalar());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SizeValue sizeValue = (SizeValue) o;

        if (size != sizeValue.size) return false;
        if (sizeUnit != sizeValue.sizeUnit) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (size ^ (size >>> 32));
        result = 31 * result + (sizeUnit != null ? sizeUnit.hashCode() : 0);
        return result;
    }
}
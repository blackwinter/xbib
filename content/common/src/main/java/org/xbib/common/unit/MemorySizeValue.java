package org.xbib.common.unit;

import java.lang.management.ManagementFactory;
import java.text.ParseException;

public final class MemorySizeValue {

    private MemorySizeValue() {
    }

    public static ByteSizeValue parseBytesSizeValueOrHeapRatio(String sValue) throws ParseException {
        if (sValue != null && sValue.endsWith("%")) {
            final String percentAsString = sValue.substring(0, sValue.length() - 1);
            try {
                final double percent = Double.parseDouble(percentAsString);
                if (percent < 0 || percent > 100) {
                    throw new ParseException("percentage should be in [0-100], got " + percentAsString, 0);
                }
                long maxheap = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getMax();
                return new ByteSizeValue((long) (percent * maxheap / 100), ByteSizeUnit.BYTES);
            } catch (NumberFormatException e) {
                throw new ParseException("failed to parse as a double: " + percentAsString, 0);
            } catch (Throwable e) {
                throw new ParseException("can not access value: " + e.getMessage(), 0);
            }
        } else {
            return ByteSizeValue.parseBytesSizeValue(sValue);
        }
    }
}

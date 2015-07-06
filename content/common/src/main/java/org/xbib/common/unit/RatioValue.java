package org.xbib.common.unit;

import java.text.ParseException;

/**
 * Utility class to represent ratio and percentage values between 0 and 100
 */
public class RatioValue {
    private final double percent;

    public RatioValue(double percent) {
        this.percent = percent;
    }

    public double getAsRatio() {
        return this.percent / 100.0;
    }

    public double getAsPercent() {
        return this.percent;
    }

    @Override
    public String toString() {
        return this.percent + "%";
    }

    /**
     * Parses the provided string as a {@link RatioValue}, the string can
     * either be in percentage format (eg. 73.5%), or a floating-point ratio
     * format (eg. 0.735)
     */
    public static RatioValue parseRatioValue(String sValue) throws ParseException {
        if (sValue.endsWith("%")) {
            final String percentAsString = sValue.substring(0, sValue.length() - 1);
            try {
                final double percent = Double.parseDouble(percentAsString);
                if (percent < 0 || percent > 100) {
                    throw new ParseException("percentage should be in [0-100], got " + percentAsString, 0);
                }
                return new RatioValue(Math.abs(percent));
            } catch (NumberFormatException e) {
                throw new ParseException("failed to parse as a double: " + percentAsString, 0);
            }
        } else {
            try {
                double ratio = Double.parseDouble(sValue);
                if (ratio < 0 || ratio > 1.0) {
                    throw new ParseException("ratio should be in [0-1.0], got " + ratio, 0);
                }
                return new RatioValue(100.0 * Math.abs(ratio));
            } catch (NumberFormatException e) {
                throw new ParseException("invalid ratio or percentage" + sValue, 0);
            }

        }
    }
}

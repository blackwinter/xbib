package org.xbib.time.pretty;

/**
 * Represents a simple method of formatting a specific {@link TimeUnitQuantity} of time
 */
public class SimpleTimeFormat implements TimeFormat {
    public static final String SIGN = "%s";
    public static final String QUANTITY = "%n";
    public static final String UNIT = "%u";
    private static final String NEGATIVE = "-";
    private String singularName = "";
    private String pluralName = "";
    private String futureSingularName = "";
    private String futurePluralName = "";
    private String pastSingularName = "";
    private String pastPluralName = "";

    private String pattern = "";
    private String futurePrefix = "";
    private String futureSuffix = "";
    private String pastPrefix = "";
    private String pastSuffix = "";
    private int roundingTolerance = 50;

    @Override
    public String format(final TimeUnitQuantity timeUnitQuantity) {
        return format(timeUnitQuantity, true);
    }

    @Override
    public String formatUnrounded(TimeUnitQuantity timeUnitQuantity) {
        return format(timeUnitQuantity, false);
    }

    @Override
    public String decorate(TimeUnitQuantity timeUnitQuantity, String time) {
        StringBuilder result = new StringBuilder();
        if (timeUnitQuantity.isInPast()) {
            result.append(pastPrefix).append(" ").append(time).append(" ").append(pastSuffix);
        } else {
            result.append(futurePrefix).append(" ").append(time).append(" ").append(futureSuffix);
        }
        return result.toString().replaceAll("\\s+", " ").trim();
    }

    @Override
    public String decorateUnrounded(TimeUnitQuantity timeUnitQuantity, String time) {
        // This format does not need to know about rounding during decoration.
        return decorate(timeUnitQuantity, time);
    }

    private String format(final TimeUnitQuantity timeUnitQuantity, final boolean round) {
        String sign = getSign(timeUnitQuantity);
        String unit = getGramaticallyCorrectName(timeUnitQuantity, round);
        long quantity = getQuantity(timeUnitQuantity, round);

        return applyPattern(sign, unit, quantity);
    }

    private String applyPattern(final String sign, final String unit, final long quantity) {
        String result = getPattern(quantity).replaceAll(SIGN, sign);
        result = result.replaceAll(QUANTITY, String.valueOf(quantity));
        result = result.replaceAll(UNIT, unit);
        return result;
    }

    protected String getPattern(final long quantity) {
        return pattern;
    }

    public String getPattern() {
        return pattern;
    }

    /*
     * Builder Setters
     */
    public SimpleTimeFormat setPattern(final String pattern) {
        this.pattern = pattern;
        return this;
    }

    protected long getQuantity(TimeUnitQuantity timeUnitQuantity, boolean round) {
        return Math.abs(round ? timeUnitQuantity.getQuantityRounded(roundingTolerance) : timeUnitQuantity.getQuantity());
    }

    protected String getGramaticallyCorrectName(final TimeUnitQuantity d, boolean round) {
        String result = getSingularName(d);
        if ((Math.abs(getQuantity(d, round)) == 0) || (Math.abs(getQuantity(d, round)) > 1)) {
            result = getPluralName(d);
        }
        return result;
    }

    private String getSign(final TimeUnitQuantity d) {
        if (d.getQuantity() < 0) {
            return NEGATIVE;
        }
        return "";
    }

    private String getSingularName(TimeUnitQuantity timeUnitQuantity) {
        if (timeUnitQuantity.isInFuture() && futureSingularName != null && futureSingularName.length() > 0) {
            return futureSingularName;
        } else if (timeUnitQuantity.isInPast() && pastSingularName != null && pastSingularName.length() > 0) {
            return pastSingularName;
        } else {
            return singularName;
        }
    }

    private String getPluralName(TimeUnitQuantity timeUnitQuantity) {
        if (timeUnitQuantity.isInFuture() && futurePluralName != null && futureSingularName.length() > 0) {
            return futurePluralName;
        } else if (timeUnitQuantity.isInPast() && pastPluralName != null && pastSingularName.length() > 0) {
            return pastPluralName;
        } else {
            return pluralName;
        }
    }

    public SimpleTimeFormat setFuturePrefix(final String futurePrefix) {
        this.futurePrefix = futurePrefix.trim();
        return this;
    }

    public SimpleTimeFormat setFutureSuffix(final String futureSuffix) {
        this.futureSuffix = futureSuffix.trim();
        return this;
    }

    public SimpleTimeFormat setPastPrefix(final String pastPrefix) {
        this.pastPrefix = pastPrefix.trim();
        return this;
    }

    public SimpleTimeFormat setPastSuffix(final String pastSuffix) {
        this.pastSuffix = pastSuffix.trim();
        return this;
    }

    /**
     * The percentage of the current {@link TimeUnit}.getMillisPerUnit() for which the quantity may be rounded up by
     * one.
     *
     * @param roundingTolerance
     * @return
     */
    public SimpleTimeFormat setRoundingTolerance(final int roundingTolerance) {
        this.roundingTolerance = roundingTolerance;
        return this;
    }

    public SimpleTimeFormat setSingularName(String name) {
        this.singularName = name;
        return this;
    }

    public SimpleTimeFormat setPluralName(String pluralName) {
        this.pluralName = pluralName;
        return this;
    }

    public SimpleTimeFormat setFutureSingularName(String futureSingularName) {
        this.futureSingularName = futureSingularName;
        return this;
    }

    public SimpleTimeFormat setFuturePluralName(String futurePluralName) {
        this.futurePluralName = futurePluralName;
        return this;
    }

    public SimpleTimeFormat setPastSingularName(String pastSingularName) {
        this.pastSingularName = pastSingularName;
        return this;
    }

    public SimpleTimeFormat setPastPluralName(String pastPluralName) {
        this.pastPluralName = pastPluralName;
        return this;
    }

    @Override
    public String toString() {
        return "SimpleTimeFormat [pattern=" + pattern + ", futurePrefix=" + futurePrefix + ", futureSuffix="
                + futureSuffix + ", pastPrefix=" + pastPrefix + ", pastSuffix=" + pastSuffix + ", roundingTolerance="
                + roundingTolerance + "]";
    }
}

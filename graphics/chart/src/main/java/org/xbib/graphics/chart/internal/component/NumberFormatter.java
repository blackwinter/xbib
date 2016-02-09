package org.xbib.graphics.chart.internal.component;

import org.xbib.graphics.chart.internal.component.Axis.Direction;
import org.xbib.graphics.chart.internal.style.StylerAxesChart;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class NumberFormatter {

    private final StylerAxesChart styler;

    /**
     * Constructor
     */
    public NumberFormatter(StylerAxesChart styler) {

        this.styler = styler;
    }

    public String getFormatPattern(BigDecimal value, double min, double max) {

        // some special cases first
        if (value.compareTo(BigDecimal.ZERO) == 0) {
            return "0";
        }

        double difference = max - min;
        int placeOfDifference;
        if (difference == 0.0) {
            placeOfDifference = 0;
        } else {
            placeOfDifference = (int) Math.floor(Math.log(difference) / Math.log(10));
        }
        int placeOfValue;
        if (value.doubleValue() == 0.0) {
            placeOfValue = 0;
        } else {
            placeOfValue = (int) Math.floor(Math.log(value.doubleValue()) / Math.log(10));
        }

        if (placeOfDifference <= 4 && placeOfDifference >= -4) {
            return getNormalDecimalPatternPositive(placeOfValue, placeOfDifference);
        } else {
            return getScientificDecimalPattern();
        }
    }

    private String getNormalDecimalPatternPositive(int placeOfValue, int placeOfDifference) {

        int maxNumPlaces = 15;
        StringBuilder sb = new StringBuilder();
        for (int i = maxNumPlaces - 1; i >= -1 * maxNumPlaces; i--) {

            if (i >= 0 && (i < placeOfValue)) {
                sb.append("0");
            } else if (i < 0 && (i > placeOfValue)) {
                sb.append("0");
            } else {
                sb.append("#");
            }
            if (i % 3 == 0 && i > 0) {
                sb.append(",");
            }
            if (i == 0) {
                sb.append(".");
            }
        }
        return sb.toString();
    }

    private String getScientificDecimalPattern() {

        return "0.###############E0";
    }

    /**
     * Format a number value, if the override patterns are null, it uses defaults
     *
     * @param value
     * @param min
     * @param max
     * @param axisDirection
     * @return
     */
    public String formatNumber(BigDecimal value, double min, double max, Direction axisDirection) {

        NumberFormat numberFormat = NumberFormat.getNumberInstance(styler.getLocale());

        String decimalPattern;

        if (axisDirection == Direction.X && styler.getXAxisDecimalPattern() != null) {

            decimalPattern = styler.getXAxisDecimalPattern();
        } else if (axisDirection == Direction.Y && styler.getYAxisDecimalPattern() != null) {
            decimalPattern = styler.getYAxisDecimalPattern();
        } else if (styler.getDecimalPattern() != null) {

            decimalPattern = styler.getDecimalPattern();
        } else {
            decimalPattern = getFormatPattern(value, min, max);
        }

        DecimalFormat normalFormat = (DecimalFormat) numberFormat;
        normalFormat.applyPattern(decimalPattern);
        return normalFormat.format(value);

    }

    /**
     * Format a log number value for log Axes which show only decade tick labels. if the override patterns are null, it
     * uses defaults
     *
     * @param value
     * @return
     */
    public String formatLogNumber(double value, Direction axisDirection) {

        NumberFormat numberFormat = NumberFormat.getNumberInstance(styler.getLocale());

        String decimalPattern;

        if (axisDirection == Direction.X && styler.getXAxisDecimalPattern() != null) {

            decimalPattern = styler.getXAxisDecimalPattern();
        } else if (axisDirection == Direction.Y && styler.getYAxisDecimalPattern() != null) {
            decimalPattern = styler.getYAxisDecimalPattern();
        } else if (styler.getDecimalPattern() != null) {

            decimalPattern = styler.getDecimalPattern();
        } else {
            if (Math.abs(value) > 1000.0 || Math.abs(value) < 0.001) {
                decimalPattern = "0E0";
            } else {
                decimalPattern = "0.###";
            }
        }

        DecimalFormat normalFormat = (DecimalFormat) numberFormat;
        normalFormat.applyPattern(decimalPattern);
        return normalFormat.format(value);

    }
}

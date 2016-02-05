package org.xbib.graphics.chart.internal.chartpart;

import org.xbib.graphics.chart.internal.Utils;
import org.xbib.graphics.chart.internal.chartpart.Axis.Direction;
import org.xbib.graphics.chart.internal.style.StylerAxesChart;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * This class encapsulates the logic to generate the axis tick mark and axis tick label data for rendering the axis
 * ticks for decimal axes
 */
public class AxisTickCalculatorNumber extends AxisTickCalculator {

    NumberFormatter numberFormatter = null;

    /**
     * Constructor
     *
     * @param axisDirection
     * @param workingSpace
     * @param minValue
     * @param maxValue
     * @param styler
     */
    public AxisTickCalculatorNumber(Direction axisDirection, double workingSpace, double minValue, double maxValue, StylerAxesChart styler) {

        super(axisDirection, workingSpace, minValue, maxValue, styler);
        numberFormatter = new NumberFormatter(styler);
        calculate();
    }

    private void calculate() {

        // a check if all axis data are the exact same values
        if (minValue == maxValue) {
            tickLabels.add(numberFormatter.formatNumber(BigDecimal.valueOf(maxValue), minValue, maxValue, axisDirection));
            tickLocations.add(workingSpace / 2.0);
            return;
        }

        // tick space - a percentage of the working space available for ticks
        double tickSpace = styler.getPlotContentSize() * workingSpace; // in plot space

        // this prevents an infinite loop when the plot gets sized really small.
        if (tickSpace < styler.getXAxisTickMarkSpacingHint()) {
            return;
        }

        // where the tick should begin in the working space in pixels
        double margin = Utils.getTickStartOffset(workingSpace, tickSpace); // in plot space double gridStep = getGridStepForDecimal(tickSpace);
        // the span of the data
        double span = Math.abs(Math.min((maxValue - minValue), Double.MAX_VALUE - 1)); // in data space

        //////////////////////////

        int tickSpacingHint = (axisDirection == Direction.X ? styler.getXAxisTickMarkSpacingHint() : styler.getYAxisTickMarkSpacingHint()) - 5;

        // for very short plots, squeeze some more ticks in than normal into the Y-Axis
        if (axisDirection == Direction.Y && tickSpace < 160) {
            tickSpacingHint = 25 - 5;
        }

        int gridStepInChartSpace = 0;

        do {

            // System.out.println("calculating ticks...");
            tickLabels.clear();
            tickLocations.clear();
            tickSpacingHint += 5;
            // System.out.println("tickSpacingHint: " + tickSpacingHint);

            double gridStepHint = span / tickSpace * tickSpacingHint;

            // gridStepHint --> significand * 10 ** exponent
            // e.g. 724.1 --> 7.241 * 10 ** 2
            double significand = gridStepHint;
            int exponent = 0;
            if (significand == 0) {
                exponent = 1;
            } else if (significand < 1) {
                while (significand < 1) {
                    significand *= 10.0;
                    exponent--;
                }
            } else {
                while (significand >= 10 || significand == Double.NEGATIVE_INFINITY) {
                    significand /= 10.0;
                    exponent++;
                }
            }

            // calculate the grid step width hint.
            double gridStep;
            if (significand > 7.5) {
                // gridStep = 10.0 * 10 ** exponent
                gridStep = 10.0 * Utils.pow(10, exponent);
            } else if (significand > 3.5) {
                // gridStep = 5.0 * 10 ** exponent
                gridStep = 5.0 * Utils.pow(10, exponent);
            } else if (significand > 1.5) {
                // gridStep = 2.0 * 10 ** exponent
                gridStep = 2.0 * Utils.pow(10, exponent);
            } else {
                // gridStep = 1.0 * 10 ** exponent
                gridStep = Utils.pow(10, exponent);
            }
            gridStepInChartSpace = (int) (gridStep / span * tickSpace);

            BigDecimal gridStepBigDecimal = BigDecimal.valueOf(gridStep);
            BigDecimal cleanedGridStep = gridStepBigDecimal.setScale(10, RoundingMode.HALF_UP).stripTrailingZeros(); // chop off any double imprecision
            BigDecimal firstPosition = null;
            firstPosition = BigDecimal.valueOf(getFirstPosition(cleanedGridStep.doubleValue()));
            BigDecimal cleanedFirstPosition = firstPosition.setScale(10, RoundingMode.HALF_UP).stripTrailingZeros(); // chop off any double imprecision

            // generate all tickLabels and tickLocations from the first to last position
            for (BigDecimal value = cleanedFirstPosition; value.compareTo(BigDecimal.valueOf(maxValue + 2 * cleanedGridStep.doubleValue())) < 0; value = value.add(cleanedGridStep)) {

                // if (value.compareTo(BigDecimal.valueOf(maxValue)) <= 0 && value.compareTo(BigDecimal.valueOf(minValue)) >= 0) {
                String tickLabel = numberFormatter.formatNumber(value, minValue, maxValue, axisDirection);
                tickLabels.add(tickLabel);

                // here we convert tickPosition finally to plot space, i.e. pixels
                double tickLabelPosition = margin + ((value.doubleValue() - minValue) / (maxValue - minValue) * tickSpace);
                tickLocations.add(tickLabelPosition);
            }
        } while (!willLabelsFitInTickSpaceHint(tickLabels, gridStepInChartSpace));

    }

}

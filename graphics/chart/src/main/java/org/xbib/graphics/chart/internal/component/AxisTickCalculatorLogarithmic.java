package org.xbib.graphics.chart.internal.component;

import org.xbib.graphics.chart.internal.Utils;
import org.xbib.graphics.chart.internal.component.Axis.Direction;
import org.xbib.graphics.chart.internal.style.StylerAxesChart;

import java.math.BigDecimal;

/**
 * This class encapsulates the logic to generate the axis tick mark and axis tick label data for rendering the axis
 * ticks for logarithmic axes
 */
public class AxisTickCalculatorLogarithmic extends AxisTickCalculator {

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
    public AxisTickCalculatorLogarithmic(Direction axisDirection, double workingSpace, double minValue, double maxValue, StylerAxesChart styler) {

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

        int logMin = (int) Math.floor(Math.log10(minValue));
        int logMax = (int) Math.ceil(Math.log10(maxValue));


        double firstPosition = Utils.pow(10, logMin);
        double tickStep = Utils.pow(10, logMin - 1);

        for (int i = logMin; i <= logMax; i++) { // for each decade

            // using the .00000001 factor to deal with double value imprecision
            for (double j = firstPosition; j <= Utils.pow(10, i) + .00000001; j = j + tickStep) {


                if (j < minValue - tickStep) {
                    // System.out.println("continue");
                    continue;
                }

                if (j > maxValue + tickStep) {
                    // System.out.println("break");
                    break;
                }

                // only add labels for the decades
                if (Math.abs(Math.log10(j) % 1) < 0.00000001) {
                    tickLabels.add(numberFormatter.formatLogNumber(j, axisDirection));
                } else {
                    tickLabels.add(null);
                }

                // add all the tick marks though
                double tickLabelPosition = (int) (margin + (Math.log10(j) - Math.log10(minValue)) / (Math.log10(maxValue) - Math.log10(minValue)) * tickSpace);
                tickLocations.add(tickLabelPosition);
            }
            tickStep = tickStep * Utils.pow(10, 1);
            firstPosition = tickStep + Utils.pow(10, i);
        }
    }
}

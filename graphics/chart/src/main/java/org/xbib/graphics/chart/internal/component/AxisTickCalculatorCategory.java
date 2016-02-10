package org.xbib.graphics.chart.internal.component;

import org.xbib.graphics.chart.internal.Utils;
import org.xbib.graphics.chart.internal.component.Axis.AxisDataType;
import org.xbib.graphics.chart.internal.component.Axis.Direction;
import org.xbib.graphics.chart.internal.style.StylerAxesChart;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * This class encapsulates the logic to generate the axis tick mark and axis tick label data for rendering the axis
 * ticks for String axes
 */
public class AxisTickCalculatorCategory extends AxisTickCalculator {

    /**
     * Constructor
     *
     * @param axisDirection
     * @param workingSpace
     * @param categories
     * @param axisType
     * @param styler
     */
    public AxisTickCalculatorCategory(Direction axisDirection, double workingSpace, List<?> categories, AxisDataType axisType, StylerAxesChart styler) {

        super(axisDirection, workingSpace, Double.NaN, Double.NaN, styler);

        calculate(categories, axisType);
    }

    private void calculate(List<?> categories, AxisDataType axisType) {

        // tick space - a percentage of the working space available for ticks
        int tickSpace = (int) (styler.getPlotContentSize() * workingSpace); // in plot space

        // where the tick should begin in the working space in pixels
        double margin = Utils.getTickStartOffset(workingSpace, tickSpace);

        // generate all tickLabels and tickLocations from the first to last position
        double gridStep = (tickSpace / (double) categories.size());
        double firstPosition = gridStep / 2.0;

        // set up String formatters that may be encountered
        NumberFormatter numberFormatter = null;
        DateTimeFormatter dateTimeFormatter = null;
        if (axisType == AxisDataType.Number) {
            numberFormatter = new NumberFormatter(styler);
        } else if (axisType == AxisDataType.Instant) {
            if (styler.getDatePattern() != null) {
                dateTimeFormatter = DateTimeFormatter.ofPattern(styler.getDatePattern())
                        .withLocale(styler.getLocale())
                        .withZone(styler.getZoneId());
            }
        }
        int counter = 0;
        for (Object category : categories) {
            if (axisType == AxisDataType.String) {
                tickLabels.add(category.toString());
                double tickLabelPosition = margin + firstPosition + gridStep * counter++;
                tickLocations.add(tickLabelPosition);
            } else if (axisType == AxisDataType.Number) {
                tickLabels.add(numberFormatter.formatNumber(new BigDecimal(category.toString()), minValue, maxValue, axisDirection));
            } else if (axisType == AxisDataType.Instant) {
                if (dateTimeFormatter != null) {
                    tickLabels.add(dateTimeFormatter.format((Instant)category));
                }
            }
            double tickLabelPosition = (int) (margin + firstPosition + gridStep * counter++);
            tickLocations.add(tickLabelPosition);
        }

    }
}

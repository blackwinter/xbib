package org.xbib.graphics.chart.internal.chartpart;

import org.xbib.graphics.chart.internal.Utils;
import org.xbib.graphics.chart.internal.chartpart.Axis.AxisDataType;
import org.xbib.graphics.chart.internal.chartpart.Axis.Direction;
import org.xbib.graphics.chart.internal.style.StylerAxesChart;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
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
        SimpleDateFormat simpleDateformat = null;
        if (axisType == AxisDataType.Number) {
            numberFormatter = new NumberFormatter(styler);
        } else if (axisType == AxisDataType.Date) {
            if (styler.getDatePattern() == null) {
                throw new RuntimeException("You need to set the Date Formatting Pattern!!!");
            }
            simpleDateformat = new SimpleDateFormat(styler.getDatePattern(), styler.getLocale());
            simpleDateformat.setTimeZone(styler.getTimezone());
        }

        int counter = 0;

        for (Object category : categories) {
            if (axisType == AxisDataType.String) {
                tickLabels.add(category.toString());
                double tickLabelPosition = margin + firstPosition + gridStep * counter++;
                tickLocations.add(tickLabelPosition);
            } else if (axisType == AxisDataType.Number) {
                tickLabels.add(numberFormatter.formatNumber(new BigDecimal(category.toString()), minValue, maxValue, axisDirection));
            } else if (axisType == AxisDataType.Date) {

                tickLabels.add(simpleDateformat.format((((Date) category).getTime())));
            }
            double tickLabelPosition = (int) (margin + firstPosition + gridStep * counter++);
            tickLocations.add(tickLabelPosition);
        }

    }
}

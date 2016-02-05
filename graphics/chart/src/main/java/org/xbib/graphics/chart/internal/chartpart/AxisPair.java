package org.xbib.graphics.chart.internal.chartpart;

import org.xbib.graphics.chart.SeriesCategory.ChartCategorySeriesRenderStyle;
import org.xbib.graphics.chart.StylerCategory;
import org.xbib.graphics.chart.internal.Series;
import org.xbib.graphics.chart.internal.SeriesAxesChart;
import org.xbib.graphics.chart.internal.style.StylerAxesChart;

import java.awt.*;
import java.awt.geom.Rectangle2D;

public class AxisPair<ST extends StylerAxesChart, S extends Series> implements ChartPart {

    private final Chart<StylerAxesChart, SeriesAxesChart> chart;

    private final Axis<StylerAxesChart, SeriesAxesChart> xAxis;
    private final Axis<StylerAxesChart, SeriesAxesChart> yAxis;

    /**
     * Constructor
     *
     * @param chart
     */
    public AxisPair(Chart<StylerAxesChart, SeriesAxesChart> chart) {

        this.chart = chart;

        // add axes
        xAxis = new Axis<StylerAxesChart, SeriesAxesChart>(chart, Axis.Direction.X);
        yAxis = new Axis<StylerAxesChart, SeriesAxesChart>(chart, Axis.Direction.Y);
    }

    @Override
    public void paint(Graphics2D g) {

        prepareForPaint();

        yAxis.paint(g);
        xAxis.paint(g);
    }

    private void prepareForPaint() {

        // set the axis data types, making sure all are compatible
        xAxis.setAxisDataType(null);
        yAxis.setAxisDataType(null);
        for (SeriesAxesChart series : chart.getSeriesMap().values()) {
            xAxis.setAxisDataType(series.getxAxisDataType());
            yAxis.setAxisDataType(series.getyAxisDataType());
        }

        // calculate axis min and max
        xAxis.resetMinMax();
        yAxis.resetMinMax();

        // if no series, we still want to plot an empty plot with axes. Since there are no min an max with no series added, we just fake it arbirarily.
        if (chart.getSeriesMap() == null || chart.getSeriesMap().size() < 1) {
            xAxis.addMinMax(-1, 1);
            yAxis.addMinMax(-1, 1);
        } else {
            for (SeriesAxesChart series : chart.getSeriesMap().values()) {
                // add min/max to axes
                // System.out.println(series.getxMin());
                // System.out.println(series.getxMax());
                // System.out.println(series.getyMin());
                // System.out.println(series.getyMax());
                // System.out.println("****");
                xAxis.addMinMax(series.getXMin(), series.getXMax());
                yAxis.addMinMax(series.getYMin(), series.getYMax());
            }
        }

        overrideMinMax();

        // logarithmic sanity check
        if (chart.getStyler().isXAxisLogarithmic() && xAxis.getMin() <= 0.0) {
            throw new IllegalArgumentException("Series data (accounting for error bars too) cannot be less or equal to zero for a logarithmic X-Axis!!!");
        }
        if (chart.getStyler().isYAxisLogarithmic() && yAxis.getMin() <= 0.0) {
            // System.out.println(getMin());
            throw new IllegalArgumentException("Series data (accounting for error bars too) cannot be less or equal to zero for a logarithmic Y-Axis!!!");
        }
    }

    public void overrideMinMax() {

        double overrideXAxisMinValue = xAxis.getMin();
        double overrideXAxisMaxValue = xAxis.getMax();
        double overrideYAxisMinValue = yAxis.getMin();
        double overrideYAxisMaxValue = yAxis.getMax();

        if (chart.getStyler() instanceof StylerCategory) {

            StylerCategory stylerCategory = (StylerCategory) chart.getStyler();
            if (stylerCategory.getDefaultSeriesRenderStyle() == ChartCategorySeriesRenderStyle.Bar) {
                // override min/max value for bar charts' Y-Axis
                // There is a special case where it's desired to anchor the axis min or max to zero, like in the case of bar charts. This flag enables that feature.
                if (yAxis.getMin() > 0.0) {
                    overrideYAxisMinValue = 0.0;
                }
                if (yAxis.getMax() < 0.0) {
                    overrideYAxisMaxValue = 0.0;
                }
            }
        }

        // override min and maxValue if specified
        if (chart.getStyler().getXAxisMin() != null)

        {
            overrideXAxisMinValue = chart.getStyler().getXAxisMin();
        }
        if (chart.getStyler().getXAxisMax() != null)

        {
            overrideXAxisMaxValue = chart.getStyler().getXAxisMax();
        }
        if (chart.getStyler().getYAxisMin() != null)

        {
            overrideYAxisMinValue = chart.getStyler().getYAxisMin();
        }
        if (chart.getStyler().getYAxisMax() != null)

        {
            overrideYAxisMaxValue = chart.getStyler().getYAxisMax();
        }

        xAxis.setMin(overrideXAxisMinValue);
        xAxis.setMax(overrideXAxisMaxValue);
        yAxis.setMin(overrideYAxisMinValue);
        yAxis.setMax(overrideYAxisMaxValue);

    }

    protected Axis<StylerAxesChart, SeriesAxesChart> getXAxis() {

        return xAxis;
    }

    protected Axis<StylerAxesChart, SeriesAxesChart> getYAxis() {

        return yAxis;
    }

    @Override
    public Rectangle2D getBounds() {

        return null; // should never be called
    }
}
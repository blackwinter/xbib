package org.xbib.graphics.chart;

import org.xbib.graphics.chart.internal.chartpart.AxisPair;
import org.xbib.graphics.chart.internal.chartpart.Chart;
import org.xbib.graphics.chart.internal.chartpart.LegendAxesChart;
import org.xbib.graphics.chart.internal.chartpart.PlotCategory;
import org.xbib.graphics.chart.internal.style.SeriesColorMarkerLineStyle;
import org.xbib.graphics.chart.internal.style.SeriesColorMarkerLineStyleCycler;
import org.xbib.graphics.chart.internal.style.Styler.ChartTheme;
import org.xbib.graphics.chart.internal.style.Theme;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.List;

public class ChartCategory extends Chart<StylerCategory, SeriesCategory> {

    /**
     * Constructor - the default Chart Theme will be used (XChartTheme)
     *
     * @param width
     * @param height
     */
    public ChartCategory(int width, int height) {

        super(width, height, new StylerCategory());
        axisPair = new AxisPair(this);
        plot = new PlotCategory(this);
        legend = new LegendAxesChart(this);
    }

    /**
     * Constructor
     *
     * @param width
     * @param height
     * @param theme  - pass in a instance of Theme class, probably a custom Theme.
     */
    public ChartCategory(int width, int height, Theme theme) {

        this(width, height);
        styler.setTheme(theme);
    }

    /**
     * Constructor
     *
     * @param width
     * @param height
     * @param chartTheme - pass in the desired ChartTheme enum
     */
    public ChartCategory(int width, int height, ChartTheme chartTheme) {

        this(width, height, chartTheme.newInstance(chartTheme));
    }

    /**
     * Constructor
     *
     * @param chartBuilder
     */
    public ChartCategory(ChartBuilderCategory chartBuilder) {

        this(chartBuilder.width, chartBuilder.height, chartBuilder.chartTheme);
        setTitle(chartBuilder.title);
        setXAxisTitle(chartBuilder.xAxisTitle);
        setYAxisTitle(chartBuilder.yAxisTitle);
    }

    /**
     * Add a series for a Category type chart using using double arrays
     *
     * @param seriesName
     * @param xData      the X-Axis data
     * @param xData      the Y-Axis data
     * @return A Series object that you can set properties on
     */
    public SeriesCategory addSeries(String seriesName, double[] xData, double[] yData) {

        return addSeries(seriesName, xData, yData, null);
    }

    /**
     * Add a series for a Category type chart using using double arrays with error bars
     *
     * @param seriesName
     * @param xData      the X-Axis data
     * @param xData      the Y-Axis data
     * @param errorBars  the error bar data
     * @return A Series object that you can set properties on
     */
    public SeriesCategory addSeries(String seriesName, double[] xData, double[] yData, double[] errorBars) {

        return addSeries(seriesName, getNumberListFromDoubleArray(xData), getNumberListFromDoubleArray(yData), getNumberListFromDoubleArray(errorBars));
    }

    /**
     * Add a series for a X-Y type chart using using int arrays
     *
     * @param seriesName
     * @param xData      the X-Axis data
     * @param xData      the Y-Axis data
     * @return A Series object that you can set properties on
     */
    public SeriesCategory addSeries(String seriesName, int[] xData, int[] yData) {

        return addSeries(seriesName, xData, yData, null);
    }

    /**
     * Add a series for a X-Y type chart using using int arrays with error bars
     *
     * @param seriesName
     * @param xData      the X-Axis data
     * @param xData      the Y-Axis data
     * @param errorBars  the error bar data
     * @return A Series object that you can set properties on
     */
    public SeriesCategory addSeries(String seriesName, int[] xData, int[] yData, int[] errorBars) {

        return addSeries(seriesName, getNumberListFromIntArray(xData), getNumberListFromIntArray(yData), getNumberListFromIntArray(errorBars));
    }

    /**
     * Add a series for a Category type chart using Lists
     *
     * @param seriesName
     * @param xData      the X-Axis data
     * @param yData      the Y-Axis data
     * @return A Series object that you can set properties on
     */
    public SeriesCategory addSeries(String seriesName, List<?> xData, List<? extends Number> yData) {

        return addSeries(seriesName, xData, yData, null);
    }

    /**
     * Add a series for a Category type chart using Lists with error bars
     *
     * @param seriesName
     * @param xData      the X-Axis data
     * @param yData      the Y-Axis data
     * @param errorBars  the error bar data
     * @return A Series object that you can set properties on
     */
    public SeriesCategory addSeries(String seriesName, List<?> xData, List<? extends Number> yData, List<? extends Number> errorBars) {

        // Sanity checks
        sanityCheck(seriesName, xData, yData, errorBars);

        SeriesCategory series = null;
        if (xData != null) {

            // Sanity check
            if (xData.size() != yData.size()) {
                throw new IllegalArgumentException("X and Y-Axis sizes are not the same!!!");
            }

            // inspect the series to see what kind of data it contains (Number, Date)

            series = new SeriesCategory(seriesName, xData, yData, errorBars);
        } else { // generate xData
            series = new SeriesCategory(seriesName, getGeneratedData(yData.size()), yData, errorBars);
        }

        seriesMap.put(seriesName, series);

        // getXAxis().setAxisType(series.getxAxisDataType());
        // getYAxis().setAxisType(AxisDataType.Number);

        return series;
    }

    ///////////////////////////////////////////////////
    // Internal Members and Methods ///////////////////
    ///////////////////////////////////////////////////

    private void sanityCheck(String seriesName, List<?> xData, List<? extends Number> yData, List<? extends Number> errorBars) {

        if (seriesMap.keySet().contains(seriesName)) {
            throw new IllegalArgumentException("Series name >" + seriesName + "< has already been used. Use unique names for each series!!!");
        }
        if (yData == null) {
            throw new IllegalArgumentException("Y-Axis data cannot be null!!!");
        }
        if (yData.size() == 0) {
            throw new IllegalArgumentException("Y-Axis data cannot be empty!!!");
        }
        if (xData != null && xData.size() == 0) {
            throw new IllegalArgumentException("X-Axis data cannot be empty!!!");
        }
        if (errorBars != null && errorBars.size() != yData.size()) {
            throw new IllegalArgumentException("Error bars and Y-Axis sizes are not the same!!!");
        }
    }

    @Override
    public void paint(Graphics2D g, int width, int height) {

        setWidth(width);
        setHeight(height);
        paint(g);
    }

    @Override
    public void paint(Graphics2D g) {

        // Sanity checks
        // if (getSeriesMap().isEmpty()) {
        // throw new RuntimeException("No series defined for Chart!!!");
        // }

        // set the series render styles if they are not set. Legend and Plot need it.
        for (SeriesCategory seriesCategory : getSeriesMap().values()) {
            SeriesCategory.ChartCategorySeriesRenderStyle seriesType = seriesCategory.getChartCategorySeriesRenderStyle(); // would be directly set
            if (seriesType == null) { // wasn't overridden, use default from Style Manager
                seriesCategory.setChartCategorySeriesRenderStyle(getStyler().getDefaultSeriesRenderStyle());
            }
        }
        setSeriesStyles();

        // paint chart main background
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // global rendering hint
        g.setColor(styler.getChartBackgroundColor());
        Shape rect = new Rectangle2D.Double(0, 0, getWidth(), getHeight());
        g.fill(rect);

        axisPair.paint(g);
        plot.paint(g);
        chartTitle.paint(g);
        legend.paint(g);

        g.dispose();
    }

    /**
     * set the series color, marker and line style based on theme
     */
    public void setSeriesStyles() {

        SeriesColorMarkerLineStyleCycler seriesColorMarkerLineStyleCycler = new SeriesColorMarkerLineStyleCycler(getStyler().getSeriesColors(), getStyler().getSeriesMarkers(), getStyler()
                .getSeriesLines());
        for (SeriesCategory series : getSeriesMap().values()) {

            SeriesColorMarkerLineStyle seriesColorMarkerLineStyle = seriesColorMarkerLineStyleCycler.getNextSeriesColorMarkerLineStyle();

            if (series.getLineStyle() == null) { // wasn't set manually
                series.setLineStyle(seriesColorMarkerLineStyle.getStroke());
            }
            if (series.getLineColor() == null) { // wasn't set manually
                series.setLineColor(seriesColorMarkerLineStyle.getColor());
            }
            if (series.getFillColor() == null) { // wasn't set manually
                series.setFillColor(seriesColorMarkerLineStyle.getColor());
            }
            if (series.getMarker() == null) { // wasn't set manually
                series.setMarker(seriesColorMarkerLineStyle.getMarker());
            }
            if (series.getMarkerColor() == null) { // wasn't set manually
                series.setMarkerColor(seriesColorMarkerLineStyle.getColor());
            }
        }
    }

}

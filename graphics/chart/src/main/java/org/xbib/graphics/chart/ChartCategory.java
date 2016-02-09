package org.xbib.graphics.chart;

import org.xbib.graphics.chart.internal.component.AxisPair;
import org.xbib.graphics.chart.internal.component.Chart;
import org.xbib.graphics.chart.internal.component.LegendAxesChart;
import org.xbib.graphics.chart.internal.component.PlotCategory;
import org.xbib.graphics.chart.internal.style.SeriesColorMarkerLineStyle;
import org.xbib.graphics.chart.internal.style.SeriesColorMarkerLineStyleCycler;
import org.xbib.graphics.chart.internal.style.Styler.ChartTheme;
import org.xbib.graphics.chart.internal.style.Theme;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.List;

public class ChartCategory extends Chart<StylerCategory, SeriesCategory> {

    public ChartCategory(int width, int height) {
        super(width, height, new StylerCategory());
        axisPair = new AxisPair(this);
        plot = new PlotCategory(this);
        legend = new LegendAxesChart(this);
    }

    public ChartCategory(int width, int height, Theme theme) {
        this(width, height);
        styler.setTheme(theme);
    }

    public ChartCategory(int width, int height, ChartTheme chartTheme) {
        this(width, height, chartTheme.newInstance(chartTheme));
    }

    public ChartCategory(ChartBuilderCategory chartBuilder) {
        this(chartBuilder.width, chartBuilder.height, chartBuilder.chartTheme);
        setTitle(chartBuilder.title);
        setXAxisTitle(chartBuilder.getxAxisTitle());
        setYAxisTitle(chartBuilder.getyAxisTitle());
    }

    /**
     * Add a series for a Category type chart using using double arrays
     *
     * @param seriesName series name
     * @param xData      the X-Axis data
     * @param yData      the Y-Axis data
     * @return A Series object that you can set properties on
     */
    public SeriesCategory addSeries(String seriesName, double[] xData, double[] yData) {
        return addSeries(seriesName, xData, yData, null);
    }

    /**
     * Add a series for a Category type chart using using double arrays with error bars
     *
     * @param seriesName series name
     * @param xData      the X-Axis data
     * @param yData      the Y-Axis data
     * @param errorBars  the error bar data
     * @return A Series object that you can set properties on
     */
    public SeriesCategory addSeries(String seriesName, double[] xData, double[] yData, double[] errorBars) {
        return addSeries(seriesName, getNumberListFromDoubleArray(xData), getNumberListFromDoubleArray(yData),
                getNumberListFromDoubleArray(errorBars));
    }

    /**
     * Add a series for a X-Y type chart using using int arrays
     *
     * @param seriesName series name
     * @param xData      the X-Axis data
     * @param yData      the Y-Axis data
     * @return A Series object that you can set properties on
     */
    public SeriesCategory addSeries(String seriesName, int[] xData, int[] yData) {
        return addSeries(seriesName, xData, yData, null);
    }

    /**
     * Add a series for a X-Y type chart using using int arrays with error bars
     *
     * @param seriesName series name
     * @param xData      the X-Axis data
     * @param yData      the Y-Axis data
     * @param errorBars  the error bar data
     * @return A Series object that you can set properties on
     */
    public SeriesCategory addSeries(String seriesName, int[] xData, int[] yData, int[] errorBars) {
        return addSeries(seriesName, getNumberListFromIntArray(xData), getNumberListFromIntArray(yData),
                getNumberListFromIntArray(errorBars));
    }

    /**
     * Add a series for a Category type chart using Lists
     *
     * @param seriesName series name
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
     * @param seriesName series name
     * @param xData      the X-Axis data
     * @param yData      the Y-Axis data
     * @param errorBars  the error bar data
     * @return A Series object that you can set properties on
     */
    public SeriesCategory addSeries(String seriesName, List<?> xData, List<? extends Number> yData,
                                    List<? extends Number> errorBars) {
        sanityCheck(seriesName, xData, yData, errorBars);
        SeriesCategory series = null;
        if (xData != null) {
            if (xData.size() != yData.size()) {
                throw new IllegalArgumentException("X and Y-Axis sizes are not the same");
            }
            series = new SeriesCategory(seriesName, xData, yData, errorBars);
        } else {
            series = new SeriesCategory(seriesName, getGeneratedData(yData.size()), yData, errorBars);
        }
        seriesMap.put(seriesName, series);
        return series;
    }

    private void sanityCheck(String seriesName, List<?> xData, List<? extends Number> yData,
                             List<? extends Number> errorBars) {
        if (seriesMap.keySet().contains(seriesName)) {
            throw new IllegalArgumentException("Series name >" + seriesName + "< has already been used");
        }
        if (yData == null) {
            throw new IllegalArgumentException("Y-Axis data cannot be null");
        }
        if (yData.size() == 0) {
            throw new IllegalArgumentException("Y-Axis data cannot be empty");
        }
        if (xData != null && xData.size() == 0) {
            throw new IllegalArgumentException("X-Axis data cannot be empty");
        }
        if (errorBars != null && errorBars.size() != yData.size()) {
            throw new IllegalArgumentException("Error bars and Y-Axis sizes are not the same");
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
        for (SeriesCategory seriesCategory : getSeriesMap().values()) {
            SeriesCategory.ChartCategorySeriesRenderStyle seriesType =
                    seriesCategory.getChartCategorySeriesRenderStyle();
            if (seriesType == null) {
                seriesCategory.setChartCategorySeriesRenderStyle(getStyler().getDefaultSeriesRenderStyle());
            }
        }
        setSeriesStyles();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(styler.getChartBackgroundColor());
        Shape rect = new Rectangle2D.Double(0, 0, getWidth(), getHeight());
        g.fill(rect);
        axisPair.paint(g);
        plot.paint(g);
        chartTitle.paint(g);
        legend.paint(g);
        g.dispose();
    }

    public void setSeriesStyles() {
        SeriesColorMarkerLineStyleCycler seriesColorMarkerLineStyleCycler =
                new SeriesColorMarkerLineStyleCycler(getStyler().getSeriesColors(),
                        getStyler().getSeriesMarkers(), getStyler().getSeriesLines());
        for (SeriesCategory series : getSeriesMap().values()) {
            SeriesColorMarkerLineStyle seriesColorMarkerLineStyle =
                    seriesColorMarkerLineStyleCycler.getNextSeriesColorMarkerLineStyle();
            if (series.getLineStyle() == null) {
                series.setLineStyle(seriesColorMarkerLineStyle.getStroke());
            }
            if (series.getLineColor() == null) {
                series.setLineColor(seriesColorMarkerLineStyle.getColor());
            }
            if (series.getFillColor() == null) {
                series.setFillColor(seriesColorMarkerLineStyle.getColor());
            }
            if (series.getMarker() == null) {
                series.setMarker(seriesColorMarkerLineStyle.getMarker());
            }
            if (series.getMarkerColor() == null) {
                series.setMarkerColor(seriesColorMarkerLineStyle.getColor());
            }
        }
    }

}

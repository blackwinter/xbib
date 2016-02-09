package org.xbib.graphics.chart;

import org.xbib.graphics.chart.internal.Series;
import org.xbib.graphics.chart.internal.component.Chart;
import org.xbib.graphics.chart.internal.component.LegendPie;
import org.xbib.graphics.chart.internal.component.PlotPie;
import org.xbib.graphics.chart.internal.style.SeriesColorMarkerLineStyle;
import org.xbib.graphics.chart.internal.style.SeriesColorMarkerLineStyleCycler;
import org.xbib.graphics.chart.internal.style.Styler.ChartTheme;
import org.xbib.graphics.chart.internal.style.Theme;

import java.awt.*;
import java.awt.geom.Rectangle2D;

public class ChartPie extends Chart<StylerPie, SeriesPie> {

    /**
     * Constructor - the default Chart Theme will be used (XChartTheme)
     *
     * @param width
     * @param height
     */
    public ChartPie(int width, int height) {

        super(width, height, new StylerPie());
        plot = new PlotPie(this);
        legend = new LegendPie(this);
    }

    /**
     * Constructor
     *
     * @param width
     * @param height
     * @param theme  - pass in a instance of Theme class, probably a custom Theme.
     */
    public ChartPie(int width, int height, Theme theme) {

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
    public ChartPie(int width, int height, ChartTheme chartTheme) {

        this(width, height, chartTheme.newInstance(chartTheme));
    }

    /**
     * Constructor
     *
     * @param chartBuilder
     */
    public ChartPie(ChartBuilderPie chartBuilder) {

        this(chartBuilder.width, chartBuilder.height, chartBuilder.chartTheme);
        setTitle(chartBuilder.title);
    }

    /**
     * Add a series for a Pie type chart
     *
     * @param seriesName
     * @param value
     * @return
     */
    public SeriesPie addSeries(String seriesName, Number value) {

        SeriesPie series = new SeriesPie(seriesName, value);

        if (seriesMap.keySet().contains(seriesName)) {
            throw new IllegalArgumentException("Series name >" + seriesName + "< has already been used. Use unique names for each series");
        }
        seriesMap.put(seriesName, series);

        return series;
    }

    @Override
    public void paint(Graphics2D g, int width, int height) {

        setWidth(width);
        setHeight(height);
        paint(g);
    }

    @Override
    public void paint(Graphics2D g) {
        for (SeriesPie seriesPie : getSeriesMap().values()) {
            SeriesPie.ChartPieSeriesRenderStyle seriesType = seriesPie.getChartPieSeriesRenderStyle(); // would be directly set
            if (seriesType == null) { // wasn't overridden, use default from Style Manager
                seriesPie.setChartPieSeriesRenderStyle(getStyler().getDefaultSeriesRenderStyle());
            }
        }
        setSeriesStyles();

        // paint chart main background
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // global rendering hint
        g.setColor(styler.getChartBackgroundColor());
        Shape rect = new Rectangle2D.Double(0, 0, getWidth(), getHeight());
        g.fill(rect);

        plot.paint(g);
        chartTitle.paint(g);
        legend.paint(g);

        g.dispose();
    }

    /**
     * set the series color based on theme
     */
    public void setSeriesStyles() {

        SeriesColorMarkerLineStyleCycler seriesColorMarkerLineStyleCycler = new SeriesColorMarkerLineStyleCycler(getStyler().getSeriesColors(), getStyler().getSeriesMarkers(), getStyler()
                .getSeriesLines());
        for (Series series : getSeriesMap().values()) {

            SeriesColorMarkerLineStyle seriesColorMarkerLineStyle = seriesColorMarkerLineStyleCycler.getNextSeriesColorMarkerLineStyle();

            if (series.getFillColor() == null) { // wasn't set manually
                series.setFillColor(seriesColorMarkerLineStyle.getColor());
            }
        }
    }

}

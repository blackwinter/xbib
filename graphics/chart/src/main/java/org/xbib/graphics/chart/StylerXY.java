package org.xbib.graphics.chart;

import org.xbib.graphics.chart.SeriesXY.ChartXYSeriesRenderStyle;
import org.xbib.graphics.chart.internal.style.StylerAxesChart;
import org.xbib.graphics.chart.internal.style.Theme;

public class StylerXY extends StylerAxesChart {

    private ChartXYSeriesRenderStyle chartXYSeriesRenderStyle;

    /**
     * Constructor
     */
    public StylerXY() {

        this.setAllStyles();
        super.setAllStyles();
    }

    @Override
    protected void setAllStyles() {

        chartXYSeriesRenderStyle = ChartXYSeriesRenderStyle.Line; // set default to line
    }

    public ChartXYSeriesRenderStyle getDefaultSeriesRenderStyle() {

        return chartXYSeriesRenderStyle;
    }

    /**
     * Sets the default series render style for the chart (line, scatter, area, etc.) You can override the series render
     * style individually on each Series object.
     *
     * @param chartXYSeriesRenderStyle
     */
    public void setDefaultSeriesRenderStyle(ChartXYSeriesRenderStyle chartXYSeriesRenderStyle) {

        this.chartXYSeriesRenderStyle = chartXYSeriesRenderStyle;
    }

    public Theme getTheme() {

        return theme;
    }

    /**
     * Set the theme the styler should use
     *
     * @param theme
     */
    protected void setTheme(Theme theme) {

        this.theme = theme;
        super.setAllStyles();
    }

}

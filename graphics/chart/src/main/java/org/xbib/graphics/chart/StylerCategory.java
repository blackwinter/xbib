package org.xbib.graphics.chart;

import org.xbib.graphics.chart.SeriesCategory.ChartCategorySeriesRenderStyle;
import org.xbib.graphics.chart.internal.style.StylerAxesChart;
import org.xbib.graphics.chart.internal.style.Theme;

public class StylerCategory extends StylerAxesChart {

    private ChartCategorySeriesRenderStyle chartCategorySeriesRenderStyle;

    // Bar Charts
    private double barWidthPercentage;
    private boolean isBarsOverlapped;

    /**
     * Constructor
     */
    public StylerCategory() {

        this.setAllStyles();
        super.setAllStyles();
    }

    @Override
    protected void setAllStyles() {

        this.chartCategorySeriesRenderStyle = ChartCategorySeriesRenderStyle.Bar; // set default to bar

        // Bar Charts
        barWidthPercentage = theme.getBarWidthPercentage();
        isBarsOverlapped = theme.isBarsOverlapped();
    }

    public ChartCategorySeriesRenderStyle getDefaultSeriesRenderStyle() {

        return chartCategorySeriesRenderStyle;
    }

    /**
     * Sets the default series render style for the chart (bar, stick, line, scatter, area, etc.) You can override the
     * series render style individually on each Series object.
     *
     * @param chartCategorySeriesRenderStyle
     */
    public void setDefaultSeriesRenderStyle(ChartCategorySeriesRenderStyle chartCategorySeriesRenderStyle) {

        this.chartCategorySeriesRenderStyle = chartCategorySeriesRenderStyle;
    }

    // Bar Charts ///////////////////////////////

    public double getBarWidthPercentage() {

        return barWidthPercentage;
    }

    /**
     * set the width of a single bar in a bar chart. full width is 100%, i.e. 1.0
     *
     * @param barWidthPercentage
     */
    public void setBarWidthPercentage(double barWidthPercentage) {

        this.barWidthPercentage = barWidthPercentage;
    }

    public boolean isBarsOverlapped() {

        return isBarsOverlapped;
    }

    /**
     * set whether or no bars are overlapped. Otherwise they are places side-by-side
     *
     * @param isBarsOverlapped
     */
    public void setBarsOverlapped(boolean isBarsOverlapped) {

        this.isBarsOverlapped = isBarsOverlapped;
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

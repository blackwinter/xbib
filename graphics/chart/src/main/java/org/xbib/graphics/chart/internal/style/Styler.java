package org.xbib.graphics.chart.internal.style;

import org.xbib.graphics.chart.internal.style.markers.Marker;

import java.awt.*;

/**
 * The styler is used to manage all things related to styling of the vast number of Chart components
 */
public abstract class Styler {

    /**
     * the default Theme
     */
    protected Theme theme = new ThemeXChart();
    // Chart Style ///////////////////////////////
    private Color chartBackgroundColor;
    private Color chartFontColor;
    private int chartPadding;
    private Color[] seriesColors;
    private BasicStroke[] seriesLines;
    private Marker[] seriesMarkers;
    // Chart Title ///////////////////////////////
    private Font chartTitleFont;
    private boolean isChartTitleVisible;
    private boolean isChartTitleBoxVisible;
    private Color chartTitleBoxBackgroundColor;
    private Color chartTitleBoxBorderColor;
    private int chartTitlePadding;
    // Chart Legend ///////////////////////////////
    private boolean isLegendVisible;
    private Color legendBackgroundColor;
    private Color legendBorderColor;
    private Font legendFont;
    private int legendPadding;
    private int legendSeriesLineLength;
    private LegendPosition legendPosition;
    // Chart Plot Area ///////////////////////////////
    private Color plotBackgroundColor;
    private Color plotBorderColor;
    private boolean isPlotBorderVisible;
    private double plotContentSize = .92;

    protected void setAllStyles() {

        // Chart Style ///////////////////////////////
        chartBackgroundColor = theme.getChartBackgroundColor();
        chartFontColor = theme.getChartFontColor();
        chartPadding = theme.getChartPadding();
        seriesColors = theme.getSeriesColors();
        seriesLines = theme.getSeriesLines();
        seriesMarkers = theme.getSeriesMarkers();

        // Chart Title ///////////////////////////////
        chartTitleFont = theme.getChartTitleFont();
        isChartTitleVisible = theme.isChartTitleVisible();
        isChartTitleBoxVisible = theme.isChartTitleBoxVisible();
        chartTitleBoxBackgroundColor = theme.getChartTitleBoxBackgroundColor();
        chartTitleBoxBorderColor = theme.getChartTitleBoxBorderColor();
        chartTitlePadding = theme.getChartTitlePadding();

        // legend
        isLegendVisible = theme.isLegendVisible();
        legendBackgroundColor = theme.getLegendBackgroundColor();
        legendBorderColor = theme.getLegendBorderColor();
        legendFont = theme.getLegendFont();
        legendPadding = theme.getLegendPadding();
        legendSeriesLineLength = theme.getLegendSeriesLineLength();
        legendPosition = theme.getLegendPosition();

        // Chart Plot Area ///////////////////////////////
        plotBackgroundColor = theme.getPlotBackgroundColor();
        plotBorderColor = theme.getPlotBorderColor();
        isPlotBorderVisible = theme.isPlotBorderVisible();
        plotContentSize = theme.getPlotContentSize();
    }

    public Color getChartBackgroundColor() {

        return chartBackgroundColor;
    }

    /**
     * Set the chart background color - the part around the edge of the chart
     *
     * @param color
     */
    public void setChartBackgroundColor(Color color) {

        this.chartBackgroundColor = color;
    }

    public Color getChartFontColor() {

        return chartFontColor;
    }

    // Chart Style ///////////////////////////////

    /**
     * Set the chart font color. includes: Chart title, axes label, legend
     *
     * @param color
     */
    public void setChartFontColor(Color color) {

        this.chartFontColor = color;
    }

    public int getChartPadding() {

        return chartPadding;
    }

    /**
     * Set the chart padding
     *
     * @param chartPadding
     */
    public void setChartPadding(int chartPadding) {

        this.chartPadding = chartPadding;
    }

    public Color[] getSeriesColors() {

        return seriesColors;
    }

    public void setSeriesColors(Color[] seriesColors) {

        this.seriesColors = seriesColors;
    }

    public BasicStroke[] getSeriesLines() {

        return seriesLines;
    }

    // Chart Title ///////////////////////////////

    public void setSeriesLines(BasicStroke[] seriesLines) {

        this.seriesLines = seriesLines;
    }

    public Marker[] getSeriesMarkers() {

        return seriesMarkers;
    }

    public void setSeriesMarkers(Marker[] seriesMarkers) {

        this.seriesMarkers = seriesMarkers;
    }

    public Font getChartTitleFont() {

        return chartTitleFont;
    }

    /**
     * Set the chart title font
     *
     * @param font
     */
    public void setChartTitleFont(Font chartTitleFont) {

        this.chartTitleFont = chartTitleFont;
    }

    public boolean isChartTitleVisible() {

        return isChartTitleVisible;
    }

    /**
     * Set the chart title visibility
     *
     * @param isChartTitleVisible
     */
    public void setChartTitleVisible(boolean isChartTitleVisible) {

        this.isChartTitleVisible = isChartTitleVisible;
    }

    public boolean isChartTitleBoxVisible() {

        return isChartTitleBoxVisible;
    }

    /**
     * Set the chart title box visibility
     *
     * @param isChartTitleBoxVisible
     */
    public void setChartTitleBoxVisible(boolean isChartTitleBoxVisible) {

        this.isChartTitleBoxVisible = isChartTitleBoxVisible;
    }

    public Color getChartTitleBoxBackgroundColor() {

        return chartTitleBoxBackgroundColor;
    }

    /**
     * set the chart title box background color
     *
     * @param chartTitleBoxBackgroundColor
     */
    public void setChartTitleBoxBackgroundColor(Color chartTitleBoxBackgroundColor) {

        this.chartTitleBoxBackgroundColor = chartTitleBoxBackgroundColor;
    }

    public Color getChartTitleBoxBorderColor() {

        return chartTitleBoxBorderColor;
    }

    /**
     * set the chart title box border color
     *
     * @param chartTitleBoxBorderColor
     */
    public void setChartTitleBoxBorderColor(Color chartTitleBoxBorderColor) {

        this.chartTitleBoxBorderColor = chartTitleBoxBorderColor;
    }

    public int getChartTitlePadding() {

        return chartTitlePadding;
    }

    /**
     * set the chart title padding; the space between the chart title and the plot area
     *
     * @param chartTitlePadding
     */
    public void setChartTitlePadding(int chartTitlePadding) {

        this.chartTitlePadding = chartTitlePadding;
    }

    public Color getLegendBackgroundColor() {

        return legendBackgroundColor;
    }

    /**
     * Set the chart legend background color
     *
     * @param color
     */
    public void setLegendBackgroundColor(Color color) {

        this.legendBackgroundColor = color;
    }

    /**
     * Set the chart legend border color
     *
     * @return
     */
    public Color getLegendBorderColor() {

        return legendBorderColor;
    }

    // Chart Legend ///////////////////////////////

    public void setLegendBorderColor(Color legendBorderColor) {

        this.legendBorderColor = legendBorderColor;
    }

    public Font getLegendFont() {

        return legendFont;
    }

    /**
     * Set the chart legend font
     *
     * @param font
     */
    public void setLegendFont(Font font) {

        this.legendFont = font;
    }

    public boolean isLegendVisible() {

        return isLegendVisible;
    }

    /**
     * Set the chart legend visibility
     *
     * @param isLegendVisible
     */
    public void setLegendVisible(boolean isLegendVisible) {

        this.isLegendVisible = isLegendVisible;
    }

    public int getLegendPadding() {

        return legendPadding;
    }

    /**
     * Set the chart legend padding
     *
     * @param legendPadding
     */
    public void setLegendPadding(int legendPadding) {

        this.legendPadding = legendPadding;
    }

    public int getLegendSeriesLineLength() {

        return legendSeriesLineLength;
    }

    /**
     * Set the chart legend series line length
     *
     * @param legendPadding
     */
    public void setLegendSeriesLineLength(int legendSeriesLineLength) {

        if (legendSeriesLineLength < 0) {
            this.legendSeriesLineLength = 0;
        } else {
            this.legendSeriesLineLength = legendSeriesLineLength;
        }
    }

    public LegendPosition getLegendPosition() {

        return legendPosition;
    }

    /**
     * sets the legend position
     *
     * @param legendPosition
     */
    public void setLegendPosition(LegendPosition legendPosition) {

        this.legendPosition = legendPosition;
    }

    public Color getPlotBackgroundColor() {

        return plotBackgroundColor;
    }

    /**
     * set the plot area's background color
     *
     * @param plotBackgroundColor
     */
    public void setPlotBackgroundColor(Color plotBackgroundColor) {

        this.plotBackgroundColor = plotBackgroundColor;
    }

    public Color getPlotBorderColor() {

        return plotBorderColor;
    }

    // Chart Plot ///////////////////////////////

    /**
     * set the plot area's border color
     *
     * @param plotBorderColor
     */
    public void setPlotBorderColor(Color plotBorderColor) {

        this.plotBorderColor = plotBorderColor;
    }

    public boolean isPlotBorderVisible() {

        return isPlotBorderVisible;
    }

    /**
     * sets the visibility of the border around the plot area
     *
     * @param isPlotBorderVisible
     */
    public void setPlotBorderVisible(boolean isPlotBorderVisible) {

        this.isPlotBorderVisible = isPlotBorderVisible;
    }

    public double getPlotContentSize() {

        return plotContentSize;
    }

    /**
     * Sets the content size of the plot inside the plot area of the chart. To fill the area 100%, use a value of 1.0.
     *
     * @param plotContentSize - Valid range is between 0 and 1.
     */
    public void setPlotContentSize(double plotContentSize) {

        if (plotContentSize < 0 || plotContentSize > 1) {
            throw new IllegalArgumentException("Plot content size must be tween 0 and 1!!!");
        }

        this.plotContentSize = plotContentSize;
    }

    public enum LegendPosition {

        OutsideE, InsideNW, InsideNE, InsideSE, InsideSW, InsideN
    }

    public enum ChartTheme {

        XChart, GGPlot2, Matlab;

        public Theme newInstance(ChartTheme chartTheme) {

            switch (chartTheme) {
                case GGPlot2:
                    return new ThemeGGPlot2();

                case Matlab:
                    return new ThemeMatlab();

                case XChart:
                default:
                    return new ThemeXChart();
            }
        }
    }

    public enum TextAlignment {

        Left, Centre, Right;
    }

}

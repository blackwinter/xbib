package org.xbib.graphics.chart.internal.style;

import java.awt.*;
import java.time.ZoneId;
import java.util.Locale;

public abstract class StylerAxesChart extends Styler {

    private boolean xAxisTitleVisible;
    private boolean yAxisTitleVisible;
    private Font axisTitleFont;
    private boolean xAxisTicksVisible;
    private boolean yAxisTicksVisible;
    private Font axisTickLabelsFont;
    private int axisTickMarkLength;
    private int axisTickPadding;
    private Color axisTickMarksColor;
    private Stroke axisTickMarksStroke;
    private Color axisTickLabelsColor;
    private boolean isAxisTicksLineVisible;
    private boolean isAxisTicksMarksVisible;
    private int plotMargin;
    private int axisTitlePadding;
    private int xAxisTickMarkSpacingHint;
    private int yAxisTickMarkSpacingHint;
    private boolean isXAxisLogarithmic;
    private boolean isYAxisLogarithmic;
    private Double xAxisMin;
    private Double xAxisMax;
    private Double yAxisMin;
    private Double yAxisMax;
    private TextAlignment xAxisLabelAlignment = TextAlignment.Centre;
    private TextAlignment yAxisLabelAlignment = TextAlignment.Left;
    private int xAxisLabelRotation = 0;

    // Chart Plot Area
    private boolean isPlotGridHorizontalLinesVisible;
    private boolean isPlotGridVerticalLinesVisible;
    private boolean isPlotTicksMarksVisible;
    private Color plotGridLinesColor;
    private Stroke plotGridLinesStroke;

    private int markerSize;

    private Color errorBarsColor;
    private boolean isErrorBarsColorSeriesColor;

    private Locale locale;
    private ZoneId zoneId;
    private String datePattern;
    private String decimalPattern;
    private String xAxisDecimalPattern;
    private String yAxisDecimalPattern;

    @Override
    protected void setAllStyles() {

        super.setAllStyles();

        // axes
        xAxisTitleVisible = theme.isXAxisTitleVisible();
        yAxisTitleVisible = theme.isYAxisTitleVisible();
        axisTitleFont = theme.getAxisTitleFont();
        xAxisTicksVisible = theme.isXAxisTicksVisible();
        yAxisTicksVisible = theme.isYAxisTicksVisible();
        axisTickLabelsFont = theme.getAxisTickLabelsFont();
        axisTickMarkLength = theme.getAxisTickMarkLength();
        axisTickPadding = theme.getAxisTickPadding();
        axisTickMarksColor = theme.getAxisTickMarksColor();
        axisTickMarksStroke = theme.getAxisTickMarksStroke();
        axisTickLabelsColor = theme.getAxisTickLabelsColor();
        isAxisTicksLineVisible = theme.isAxisTicksLineVisible();
        isAxisTicksMarksVisible = theme.isAxisTicksMarksVisible();
        plotMargin = theme.getPlotMargin();
        axisTitlePadding = theme.getAxisTitlePadding();
        xAxisTickMarkSpacingHint = theme.getXAxisTickMarkSpacingHint();
        yAxisTickMarkSpacingHint = theme.getYAxisTickMarkSpacingHint();
        isXAxisLogarithmic = false;
        isYAxisLogarithmic = false;
        xAxisMin = null;
        xAxisMax = null;
        yAxisMin = null;
        yAxisMax = null;

        isPlotGridVerticalLinesVisible = theme.isPlotGridVerticalLinesVisible();
        isPlotGridHorizontalLinesVisible = theme.isPlotGridHorizontalLinesVisible();
        isPlotTicksMarksVisible = theme.isPlotTicksMarksVisible();
        plotGridLinesColor = theme.getPlotGridLinesColor();
        plotGridLinesStroke = theme.getPlotGridLinesStroke();

        markerSize = theme.getMarkerSize();

        errorBarsColor = theme.getErrorBarsColor();
        isErrorBarsColorSeriesColor = theme.isErrorBarsColorSeriesColor();

        locale = Locale.getDefault();
        zoneId = ZoneId.of("UTC");
        datePattern = null; // if not null, this override pattern will be used
        decimalPattern = null;
        xAxisDecimalPattern = null;
        yAxisDecimalPattern = null;
    }

    public boolean isXAxisTitleVisible() {
        return xAxisTitleVisible;
    }

    /**
     * Set the x-axis title visibility
     *
     * @param xAxisTitleVisible
     */
    public void setXAxisTitleVisible(boolean xAxisTitleVisible) {
        this.xAxisTitleVisible = xAxisTitleVisible;
    }

    public boolean isYAxisTitleVisible() {
        return yAxisTitleVisible;
    }

    /**
     * Set the y-axis title visibility
     *
     * @param yAxisTitleVisible
     */
    public void setYAxisTitleVisible(boolean yAxisTitleVisible) {
        this.yAxisTitleVisible = yAxisTitleVisible;
    }

    /**
     * Set the x- and y-axis titles visibility
     *
     * @param isVisible
     */
    public void setAxisTitlesVisible(boolean isVisible) {
        this.xAxisTitleVisible = isVisible;
        this.yAxisTitleVisible = isVisible;
    }

    public Font getAxisTitleFont() {
        return axisTitleFont;
    }

    /**
     * Set the x- and y-axis title font
     *
     * @param axisTitleFont
     */
    public void setAxisTitleFont(Font axisTitleFont) {
        this.axisTitleFont = axisTitleFont;
    }

    public boolean isXAxisTicksVisible() {
        return xAxisTicksVisible;
    }

    /**
     * Set the x-axis tick marks and labels visibility
     *
     * @param xAxisTicksVisible
     */
    public void setXAxisTicksVisible(boolean xAxisTicksVisible) {
        this.xAxisTicksVisible = xAxisTicksVisible;
    }

    public boolean isYAxisTicksVisible() {
        return yAxisTicksVisible;
    }

    /**
     * Set the y-axis tick marks and labels visibility
     *
     * @param yAxisTicksVisible
     */
    public void setYAxisTicksVisible(boolean yAxisTicksVisible) {
        this.yAxisTicksVisible = yAxisTicksVisible;
    }

    /**
     * Set the x- and y-axis tick marks and labels visibility
     *
     * @param isVisible
     */
    public void setAxisTicksVisible(boolean isVisible) {
        this.xAxisTicksVisible = isVisible;
        this.yAxisTicksVisible = isVisible;
    }

    public Font getAxisTickLabelsFont() {
        return axisTickLabelsFont;
    }

    /**
     * Set the x- and y-axis tick label font
     *
     * @param axisTicksFont
     */
    public void setAxisTickLabelsFont(Font axisTicksFont) {
        this.axisTickLabelsFont = axisTicksFont;
    }

    public int getAxisTickMarkLength() {
        return axisTickMarkLength;
    }

    /**
     * Set the axis tick mark length
     *
     * @param axisTickMarkLength
     */
    public void setAxisTickMarkLength(int axisTickMarkLength) {
        this.axisTickMarkLength = axisTickMarkLength;
    }

    public int getAxisTickPadding() {
        return axisTickPadding;
    }

    /**
     * sets the padding between the tick labels and the tick marks
     *
     * @param axisTickPadding
     */
    public void setAxisTickPadding(int axisTickPadding) {
        this.axisTickPadding = axisTickPadding;
    }

    public Color getAxisTickMarksColor() {
        return axisTickMarksColor;
    }

    /**
     * sets the axis tick mark color
     *
     * @param axisTickColor
     */
    public void setAxisTickMarksColor(Color axisTickColor) {
        this.axisTickMarksColor = axisTickColor;
    }

    public Stroke getAxisTickMarksStroke() {
        return axisTickMarksStroke;
    }

    /**
     * sets the axis tick marks Stroke
     *
     * @param axisTickMarksStroke
     */
    public void setAxisTickMarksStroke(Stroke axisTickMarksStroke) {
        this.axisTickMarksStroke = axisTickMarksStroke;
    }

    public Color getAxisTickLabelsColor() {
        return axisTickLabelsColor;
    }

    /**
     * sets the axis tick label color
     *
     * @param axisTickLabelsColor
     */
    public void setAxisTickLabelsColor(Color axisTickLabelsColor) {
        this.axisTickLabelsColor = axisTickLabelsColor;
    }

    public boolean isAxisTicksLineVisible() {
        return isAxisTicksLineVisible;
    }

    /**
     * sets the visibility of the line parallel to the plot edges that go along with the tick marks
     *
     * @param isAxisTicksLineVisible
     */
    public void setAxisTicksLineVisible(boolean isAxisTicksLineVisible) {
        this.isAxisTicksLineVisible = isAxisTicksLineVisible;
    }

    public boolean isAxisTicksMarksVisible() {
        return isAxisTicksMarksVisible;
    }

    /**
     * sets the visibility of the tick marks
     *
     * @param isAxisTicksMarksVisible
     */
    public void setAxisTicksMarksVisible(boolean isAxisTicksMarksVisible) {
        this.isAxisTicksMarksVisible = isAxisTicksMarksVisible;
    }

    public int getPlotMargin() {
        return plotMargin;
    }

    /**
     * sets the margin around the plot area
     *
     * @param plotMargin
     */
    public void setPlotMargin(int plotMargin) {
        this.plotMargin = plotMargin;
    }

    public int getAxisTitlePadding() {
        return axisTitlePadding;
    }

    /**
     * sets the padding between the axis title and the tick labels
     *
     * @param axisTitlePadding
     */
    public void setAxisTitlePadding(int axisTitlePadding) {
        this.axisTitlePadding = axisTitlePadding;
    }

    public int getXAxisTickMarkSpacingHint() {
        return xAxisTickMarkSpacingHint;
    }

    /**
     * set the spacing between tick marks for the X-Axis
     *
     * @param xAxisTickMarkSpacingHint
     */
    public void setXAxisTickMarkSpacingHint(int xAxisTickMarkSpacingHint) {
        this.xAxisTickMarkSpacingHint = xAxisTickMarkSpacingHint;
    }

    public int getYAxisTickMarkSpacingHint() {
        return yAxisTickMarkSpacingHint;
    }

    /**
     * set the spacing between tick marks for the Y-Axis
     *
     * @param yAxisTickMarkSpacingHint
     */
    public void setYAxisTickMarkSpacingHint(int yAxisTickMarkSpacingHint) {
        this.yAxisTickMarkSpacingHint = yAxisTickMarkSpacingHint;
    }

    public boolean isXAxisLogarithmic() {

        return isXAxisLogarithmic;
    }

    /**
     * sets the X-Axis to be rendered with a logarithmic scale or not
     *
     * @param isXAxisLogarithmic
     */
    public void setXAxisLogarithmic(boolean isXAxisLogarithmic) {
        this.isXAxisLogarithmic = isXAxisLogarithmic;
    }

    public boolean isYAxisLogarithmic() {
        return isYAxisLogarithmic;
    }

    /**
     * sets the Y-Axis to be rendered with a logarithmic scale or not
     *
     * @param isYAxisLogarithmic
     */
    public void setYAxisLogarithmic(boolean isYAxisLogarithmic) {
        this.isYAxisLogarithmic = isYAxisLogarithmic;
    }

    public Double getXAxisMin() {
        return xAxisMin;
    }

    public void setXAxisMin(double xAxisMin) {
        this.xAxisMin = xAxisMin;
    }

    public Double getXAxisMax() {
        return xAxisMax;
    }

    public void setXAxisMax(double xAxisMax) {
        this.xAxisMax = xAxisMax;
    }

    public Double getYAxisMin() {
        return yAxisMin;
    }

    public void setYAxisMin(double yAxisMin) {
        this.yAxisMin = yAxisMin;
    }

    public Double getYAxisMax() {
        return yAxisMax;
    }

    public void setYAxisMax(double yAxisMax) {
        this.yAxisMax = yAxisMax;
    }

    public TextAlignment getXAxisLabelAlignment() {
        return xAxisLabelAlignment;
    }

    public void setXAxisLabelAlignment(TextAlignment xAxisLabelAlignment) {
        this.xAxisLabelAlignment = xAxisLabelAlignment;
    }

    public TextAlignment getYAxisLabelAlignment() {
        return yAxisLabelAlignment;
    }

    public void setYAxisLabelAlignment(TextAlignment yAxisLabelAlignment) {
        this.yAxisLabelAlignment = yAxisLabelAlignment;
    }

    public int getXAxisLabelRotation() {
        return xAxisLabelRotation;
    }

    public void setXAxisLabelRotation(int xAxisLabelRotation) {
        this.xAxisLabelRotation = xAxisLabelRotation;
    }

    public boolean isPlotGridLinesVisible() {
        return isPlotGridHorizontalLinesVisible && isPlotGridVerticalLinesVisible;
    }

    /**
     * sets the visibility of the gridlines on the plot area
     *
     * @param isPlotGridLinesVisible
     */
    public void setPlotGridLinesVisible(boolean isPlotGridLinesVisible) {
        this.isPlotGridHorizontalLinesVisible = isPlotGridLinesVisible;
        this.isPlotGridVerticalLinesVisible = isPlotGridLinesVisible;
    }

    public boolean isPlotGridHorizontalLinesVisible() {
        return isPlotGridHorizontalLinesVisible;
    }

    /**
     * sets the visibility of the horizontal gridlines on the plot area
     *
     * @param isPlotGridHorizontalLinesVisible
     */
    public void setPlotGridHorizontalLinesVisible(boolean isPlotGridHorizontalLinesVisible) {
        this.isPlotGridHorizontalLinesVisible = isPlotGridHorizontalLinesVisible;
    }

    public boolean isPlotGridVerticalLinesVisible() {
        return isPlotGridVerticalLinesVisible;
    }

    /**
     * sets the visibility of the vertical gridlines on the plot area
     *
     * @param isPlotGridVerticalLinesVisible
     */
    public void setPlotGridVerticalLinesVisible(boolean isPlotGridVerticalLinesVisible) {
        this.isPlotGridVerticalLinesVisible = isPlotGridVerticalLinesVisible;
    }

    public boolean isPlotTicksMarksVisible() {
        return isPlotTicksMarksVisible;
    }

    /**
     * sets the visibility of the ticks marks inside the plot area
     *
     * @param isPlotTicksMarksVisible
     */
    public void setPlotTicksMarksVisible(boolean isPlotTicksMarksVisible) {
        this.isPlotTicksMarksVisible = isPlotTicksMarksVisible;
    }

    public Color getPlotGridLinesColor() {
        return plotGridLinesColor;
    }

    /**
     * set the plot area's grid lines color
     *
     * @param plotGridLinesColor
     */
    public void setPlotGridLinesColor(Color plotGridLinesColor) {
        this.plotGridLinesColor = plotGridLinesColor;
    }

    public Stroke getPlotGridLinesStroke() {
        return plotGridLinesStroke;
    }

    /**
     * set the plot area's grid lines Stroke
     *
     * @param plotGridLinesStroke
     */
    public void setPlotGridLinesStroke(Stroke plotGridLinesStroke) {
        this.plotGridLinesStroke = plotGridLinesStroke;
    }


    public int getMarkerSize() {
        return markerSize;
    }

    /**
     * Sets the size of the markers in pixels
     *
     * @param markerSize
     */
    public void setMarkerSize(int markerSize) {
        this.markerSize = markerSize;
    }


    public Color getErrorBarsColor() {
        return errorBarsColor;
    }

    /**
     * Sets the color of the error bars
     *
     * @param errorBarsColor
     */
    public void setErrorBarsColor(Color errorBarsColor) {
        this.errorBarsColor = errorBarsColor;
    }

    public boolean isErrorBarsColorSeriesColor() {
        return isErrorBarsColorSeriesColor;
    }

    /**
     * Set true if the the error bar color should match the series color
     *
     * @return
     */
    public void setErrorBarsColorSeriesColor(boolean isErrorBarsColorSeriesColor) {
        this.isErrorBarsColorSeriesColor = isErrorBarsColorSeriesColor;
    }

    public Locale getLocale() {
        return locale;
    }

    /**
     * Set the locale to use for rendering the chart
     *
     * @param locale - the locale to use when formatting Strings and dates for the axis tick labels
     */
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public ZoneId getZoneId() {
        return zoneId;
    }

    /**
     * Set the zone ID to use for formatting time instant axis tick labels
     *
     * @param zoneId the zone ID to use when formatting time instants
     */
    public void setZoneId(ZoneId zoneId) {
        this.zoneId = zoneId;
    }

    public String getDatePattern() {
        return datePattern;
    }

    /**
     * Set the String formatter for Data x-axis
     *
     * @param datePattern - the pattern describing the date and time format
     */
    public void setDatePattern(String datePattern) {
        this.datePattern = datePattern;
    }

    public String getDecimalPattern() {
        return decimalPattern;
    }

    /**
     * Set the decimal formatter for all tick labels
     *
     * @param decimalPattern - the pattern describing the decimal format
     */
    public void setDecimalPattern(String decimalPattern) {
        this.decimalPattern = decimalPattern;
    }

    public String getXAxisDecimalPattern() {
        return xAxisDecimalPattern;
    }

    /**
     * Set the decimal formatting pattern for the X-Axis
     *
     * @param xAxisDecimalPattern
     */
    public void setXAxisDecimalPattern(String xAxisDecimalPattern) {
        this.xAxisDecimalPattern = xAxisDecimalPattern;
    }

    public String getYAxisDecimalPattern() {
        return yAxisDecimalPattern;
    }

    /**
     * Set the decimal formatting pattern for the Y-Axis
     *
     * @param yAxisDecimalPattern
     */
    public void setYAxisDecimalPattern(String yAxisDecimalPattern) {
        this.yAxisDecimalPattern = yAxisDecimalPattern;
    }

}

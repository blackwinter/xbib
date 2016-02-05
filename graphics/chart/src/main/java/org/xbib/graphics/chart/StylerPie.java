package org.xbib.graphics.chart;

import org.xbib.graphics.chart.SeriesPie.ChartPieSeriesRenderStyle;
import org.xbib.graphics.chart.internal.style.Styler;
import org.xbib.graphics.chart.internal.style.Theme;

import java.awt.*;

public class StylerPie extends Styler {

    private ChartPieSeriesRenderStyle chartPieSeriesRenderStyle;
    private boolean isCircular;
    private double startAngleInDegrees;
    private Font annotationFont;
    private double annotationDistance;
    private AnnotationType annotationType;
    /**
     * Constructor
     */
    public StylerPie() {

        this.setAllStyles();
        super.setAllStyles();
    }

    @Override
    protected void setAllStyles() {

        chartPieSeriesRenderStyle = ChartPieSeriesRenderStyle.Pie; // set default to pie, donut may be a future one
        isCircular = theme.isCircular();
        annotationFont = theme.getPieFont();
        annotationDistance = theme.getAnnotationDistance();
        annotationType = theme.getAnnotationType();
    }

    public ChartPieSeriesRenderStyle getDefaultSeriesRenderStyle() {

        return chartPieSeriesRenderStyle;
    }

    /**
     * Sets the default series render style for the chart (line, scatter, area, etc.) You can override the series render
     * style individually on each Series object.
     *
     * @param chartPieSeriesRenderStyle
     */
    public void setDefaultSeriesRenderStyle(ChartPieSeriesRenderStyle chartPieSeriesRenderStyle) {

        this.chartPieSeriesRenderStyle = chartPieSeriesRenderStyle;
    }

    public boolean isCircular() {

        return isCircular;
    }

    /**
     * Sets whether or not the pie chart is forced to be circular. Otherwise it's shape is oval, matching the containing
     * plot.
     *
     * @param isCircular
     */
    public void setCircular(boolean isCircular) {

        this.isCircular = isCircular;
    }

    public double getStartAngleInDegrees() {

        return startAngleInDegrees;
    }

    /**
     * Sets the start angle in degrees. Zero degrees is straight up.
     *
     * @param startAngleInDegrees
     */
    public void setStartAngleInDegrees(double startAngleInDegrees) {

        this.startAngleInDegrees = startAngleInDegrees;
    }

    public Font getAnnotationFont() {

        return annotationFont;
    }

    /**
     * Sets the font used on the Pie Chart's annotations
     *
     * @param pieFont
     */
    public void setAnnotationFont(Font pieFont) {

        this.annotationFont = pieFont;
    }

    public double getAnnotationDistance() {

        return annotationDistance;
    }

    /**
     * Sets the distance of the pie chart's annotation where 0 is the center, 1 is at the edge and greater than 1 is
     * outside of the pie chart.
     *
     * @param annotationDistance
     */
    public void setAnnotationDistance(double annotationDistance) {

        this.annotationDistance = annotationDistance;
    }

    public AnnotationType getAnnotationType() {

        return annotationType;
    }

    /**
     * Sets the Pie chart's annotation type
     *
     * @param annotationType
     */
    public void setAnnotationType(AnnotationType annotationType) {

        this.annotationType = annotationType;
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

    public enum AnnotationType {

        Percentage, Label, LabelAndPercentage
    }

}

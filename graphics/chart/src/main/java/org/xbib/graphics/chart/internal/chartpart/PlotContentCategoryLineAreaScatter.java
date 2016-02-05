package org.xbib.graphics.chart.internal.chartpart;

import org.xbib.graphics.chart.SeriesCategory;
import org.xbib.graphics.chart.SeriesCategory.ChartCategorySeriesRenderStyle;
import org.xbib.graphics.chart.StylerCategory;
import org.xbib.graphics.chart.internal.Series;
import org.xbib.graphics.chart.internal.Utils;
import org.xbib.graphics.chart.internal.style.Styler;
import org.xbib.graphics.chart.internal.style.lines.SeriesLines;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public class PlotContentCategoryLineAreaScatter<ST extends Styler, S extends Series> extends PlotContent {

    StylerCategory stylerCategory;

    /**
     * Constructor
     *
     * @param chart
     */
    protected PlotContentCategoryLineAreaScatter(Chart<StylerCategory, SeriesCategory> chart) {

        super(chart);
        this.stylerCategory = chart.getStyler();
    }

    @Override
    public void paint(Graphics2D g) {

        Rectangle2D bounds = getBounds();
        // if the area to draw a chart on is so small, don't even bother
        if (bounds.getWidth() < 30) {
            return;
        }

        // this is for preventing the series to be drawn outside the plot area if min and max is overridden to fall inside the data range

        Rectangle2D rectangle = new Rectangle2D.Double(0, 0, chart.getWidth(), chart.getHeight());
        g.setClip(bounds.createIntersection(rectangle));

        // X-Axis
        double xTickSpace = stylerCategory.getPlotContentSize() * bounds.getWidth();
        double xLeftMargin = Utils.getTickStartOffset((int) bounds.getWidth(), xTickSpace);

        // Y-Axis
        double yTickSpace = stylerCategory.getPlotContentSize() * bounds.getHeight();
        double yTopMargin = Utils.getTickStartOffset((int) bounds.getHeight(), yTickSpace);

        double xMin = chart.getAxisPair().getXAxis().getMin();
        double xMax = chart.getAxisPair().getXAxis().getMax();
        double yMin = chart.getAxisPair().getYAxis().getMin();
        double yMax = chart.getAxisPair().getYAxis().getMax();

        // logarithmic
        if (stylerCategory.isXAxisLogarithmic()) {
            xMin = Math.log10(xMin);
            xMax = Math.log10(xMax);
        }
        if (stylerCategory.isYAxisLogarithmic()) {
            yMin = Math.log10(yMin);
            yMax = Math.log10(yMax);
        }
        // System.out.println("yMin = " + yMin);
        // System.out.println("yMax = " + yMax);

        Map<String, SeriesCategory> seriesMap = chart.getSeriesMap();

        int numCategories = seriesMap.values().iterator().next().getXData().size();
        double gridStep = xTickSpace / numCategories;

        for (SeriesCategory series : seriesMap.values()) {

            // data points
            Collection<? extends Number> yData = series.getYData();

            double previousX = -Double.MAX_VALUE;
            double previousY = -Double.MAX_VALUE;

            Iterator<? extends Number> yItr = yData.iterator();
            Iterator<? extends Number> ebItr = null;
            Collection<? extends Number> errorBars = series.getErrorBars();
            if (errorBars != null) {
                ebItr = errorBars.iterator();
            }
            Path2D.Double path = null;

            int categoryCounter = 0;
            while (yItr.hasNext()) {

                Number next = yItr.next();
                if (next == null) {

                    // for area charts
                    closePath(g, path, previousX, bounds, yTopMargin);
                    path = null;

                    previousX = -Double.MAX_VALUE;
                    previousY = -Double.MAX_VALUE;
                    continue;
                }

                double yOrig = next.doubleValue();

                double y = 0.0;

                if (stylerCategory.isYAxisLogarithmic()) {
                    y = Math.log10(yOrig);
                } else {
                    y = yOrig;
                }
                System.out.println(y);

                double yTransform = bounds.getHeight() - (yTopMargin + (y - yMin) / (yMax - yMin) * yTickSpace);

                // a check if all y data are the exact same values
                if (Math.abs(yMax - yMin) / 5 == 0.0) {
                    yTransform = bounds.getHeight() / 2.0;
                }

                double xOffset = bounds.getX() + xLeftMargin + categoryCounter++ * gridStep + gridStep / 2;
                double yOffset = bounds.getY() + yTransform;

                // paint line
                if (ChartCategorySeriesRenderStyle.Line.equals(series.getChartCategorySeriesRenderStyle()) || ChartCategorySeriesRenderStyle.Area.equals(series.getChartCategorySeriesRenderStyle())) {

                    if (series.getLineStyle() != SeriesLines.NONE) {

                        if (previousX != -Double.MAX_VALUE && previousY != -Double.MAX_VALUE) {
                            g.setColor(series.getLineColor());
                            g.setStroke(series.getLineStyle());
                            Shape line = new Line2D.Double(previousX, previousY, xOffset, yOffset);
                            g.draw(line);
                        }
                    }
                }

                // paint area
                if (ChartCategorySeriesRenderStyle.Area.equals(series.getChartCategorySeriesRenderStyle())) {

                    if (previousX != -Double.MAX_VALUE && previousY != -Double.MAX_VALUE) {

                        g.setColor(series.getFillColor());
                        double yBottomOfArea = bounds.getY() + bounds.getHeight() - yTopMargin;

                        if (path == null) {
                            path = new Path2D.Double();
                            path.moveTo(previousX, yBottomOfArea);
                            path.lineTo(previousX, previousY);
                        }
                        path.lineTo(xOffset, yOffset);
                    }
                    if (xOffset < previousX) {
                        throw new RuntimeException("X-Data must be in ascending order for Area Charts!!!");
                    }
                }

                // paint stick
                if (ChartCategorySeriesRenderStyle.Stick.equals(series.getChartCategorySeriesRenderStyle())) {

                    if (series.getLineStyle() != SeriesLines.NONE) {

                        double yBottomOfArea = bounds.getY() + bounds.getHeight() - yTopMargin;

                        g.setColor(series.getLineColor());
                        g.setStroke(series.getLineStyle());
                        Shape line = new Line2D.Double(xOffset, yBottomOfArea, xOffset, yOffset);
                        g.draw(line);
                    }

                }

                previousX = xOffset;
                previousY = yOffset;

                // paint marker
                if (series.getMarker() != null) {
                    g.setColor(series.getMarkerColor());
                    series.getMarker().paint(g, xOffset, yOffset, stylerCategory.getMarkerSize());
                }

                // paint error bars
                if (errorBars != null) {

                    double eb = ebItr.next().doubleValue();

                    // set error bar style
                    if (stylerCategory.isErrorBarsColorSeriesColor()) {
                        g.setColor(series.getLineColor());
                    } else {
                        g.setColor(stylerCategory.getErrorBarsColor());
                    }
                    g.setStroke(errorBarStroke);

                    // Top value
                    double topValue = 0.0;
                    if (stylerCategory.isYAxisLogarithmic()) {
                        topValue = yOrig + eb;
                        topValue = Math.log10(topValue);
                    } else {
                        topValue = y + eb;
                    }
                    double topEBTransform = bounds.getHeight() - (yTopMargin + (topValue - yMin) / (yMax - yMin) * yTickSpace);
                    double topEBOffset = bounds.getY() + topEBTransform;

                    // Bottom value
                    double bottomValue = 0.0;
                    if (stylerCategory.isYAxisLogarithmic()) {
                        bottomValue = yOrig - eb;
                        // System.out.println(bottomValue);
                        bottomValue = Math.log10(bottomValue);
                    } else {
                        bottomValue = y - eb;
                    }
                    double bottomEBTransform = bounds.getHeight() - (yTopMargin + (bottomValue - yMin) / (yMax - yMin) * yTickSpace);
                    double bottomEBOffset = bounds.getY() + bottomEBTransform;

                    // Draw it
                    Shape line = new Line2D.Double(xOffset, topEBOffset, xOffset, bottomEBOffset);
                    g.draw(line);
                    line = new Line2D.Double(xOffset - 3, bottomEBOffset, xOffset + 3, bottomEBOffset);
                    g.draw(line);
                    line = new Line2D.Double(xOffset - 3, topEBOffset, xOffset + 3, topEBOffset);
                    g.draw(line);
                }
            }

            // close any open path for area charts
            closePath(g, path, previousX, bounds, yTopMargin);
        }
        g.setClip(null);

    }

    /**
     * Closes a path for area charts if one is available.
     */
    private void closePath(Graphics2D g, Path2D.Double path, double previousX, Rectangle2D bounds, double yTopMargin) {

        if (path != null) {
            double yBottomOfArea = bounds.getY() + bounds.getHeight() - yTopMargin;
            path.lineTo(previousX, yBottomOfArea);
            path.closePath();
            g.fill(path);
        }
    }

}

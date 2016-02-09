package org.xbib.graphics.chart.internal.component;

import org.xbib.graphics.chart.SeriesXY;
import org.xbib.graphics.chart.SeriesXY.ChartXYSeriesRenderStyle;
import org.xbib.graphics.chart.StylerXY;
import org.xbib.graphics.chart.internal.Series;
import org.xbib.graphics.chart.internal.Utils;
import org.xbib.graphics.chart.internal.component.Axis.AxisDataType;
import org.xbib.graphics.chart.internal.style.StylerAxesChart;
import org.xbib.graphics.chart.internal.style.lines.SeriesLines;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.time.Instant;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public class PlotContentXY<ST extends StylerAxesChart, S extends Series> extends PlotContent {

    StylerXY stylerXY;

    /**
     * Constructor
     *
     * @param chart
     */
    protected PlotContentXY(Chart<StylerXY, SeriesXY> chart) {

        super(chart);
        stylerXY = chart.getStyler();
    }

    @Override
    public void paint(Graphics2D g) {

        Rectangle2D bounds = getBounds();
        if (bounds.getWidth() < 30) {
            return;
        }

        Rectangle2D rectangle = new Rectangle2D.Double(0, 0, chart.getWidth(), chart.getHeight());
        g.setClip(bounds.createIntersection(rectangle));

        double xTickSpace = stylerXY.getPlotContentSize() * bounds.getWidth();
        double xLeftMargin = Utils.getTickStartOffset((int) bounds.getWidth(), xTickSpace);
        double yTickSpace = stylerXY.getPlotContentSize() * bounds.getHeight();
        double yTopMargin = Utils.getTickStartOffset((int) bounds.getHeight(), yTickSpace);

        double xMin = chart.getXAxis().getMin();
        double xMax = chart.getXAxis().getMax();
        double yMin = chart.getYAxis().getMin();
        double yMax = chart.getYAxis().getMax();

        // logarithmic
        if (stylerXY.isXAxisLogarithmic()) {
            xMin = Math.log10(xMin);
            xMax = Math.log10(xMax);
        }
        if (stylerXY.isYAxisLogarithmic()) {
            yMin = Math.log10(yMin);
            yMax = Math.log10(yMax);
        }

        Map<String, SeriesXY> map = chart.getSeriesMap();
        for (SeriesXY series : map.values()) {

            // data points
            Collection<?> xData = series.getXData();
            Collection<? extends Number> yData = series.getYData();

            double previousX = -Double.MAX_VALUE;
            double previousY = -Double.MAX_VALUE;

            Iterator<?> xItr = xData.iterator();
            Iterator<? extends Number> yItr = yData.iterator();
            Iterator<? extends Number> ebItr = null;
            Collection<? extends Number> errorBars = series.getErrorBars();
            if (errorBars != null) {
                ebItr = errorBars.iterator();
            }
            Path2D.Double path = null;

            while (xItr.hasNext()) {

                double x = 0.0;
                if (chart.getXAxis().getAxisDataType() == AxisDataType.Number) {
                    x = ((Number) xItr.next()).doubleValue();
                } else if (chart.getXAxis().getAxisDataType() == AxisDataType.Instant) {
                    x = ((Instant) xItr.next()).toEpochMilli();
                }
                if (stylerXY.isXAxisLogarithmic()) {
                    x = Math.log10(x);
                }

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

                if (stylerXY.isYAxisLogarithmic()) {
                    y = Math.log10(yOrig);
                } else {
                    y = yOrig;
                }

                double xTransform = xLeftMargin + ((x - xMin) / (xMax - xMin) * xTickSpace);
                double yTransform = bounds.getHeight() - (yTopMargin + (y - yMin) / (yMax - yMin) * yTickSpace);

                // a check if all x data are the exact same values
                if (Math.abs(xMax - xMin) / 5 == 0.0) {
                    xTransform = bounds.getWidth() / 2.0;
                }

                // a check if all y data are the exact same values
                if (Math.abs(yMax - yMin) / 5 == 0.0) {
                    yTransform = bounds.getHeight() / 2.0;
                }

                double xOffset = bounds.getX() + xTransform;
                double yOffset = bounds.getY() + yTransform;
                // paint line

                boolean isSeriesLineOrArea = (ChartXYSeriesRenderStyle.Line == series.getChartXYSeriesRenderStyle()) || (ChartXYSeriesRenderStyle.Area == series.getChartXYSeriesRenderStyle());

                if (isSeriesLineOrArea) {
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
                if (ChartXYSeriesRenderStyle.Area == series.getChartXYSeriesRenderStyle()) {

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
                        throw new RuntimeException("X-Data must be in ascending order for Area Charts");
                    }
                }

                previousX = xOffset;
                previousY = yOffset;

                // paint marker
                if (series.getMarker() != null) { // if set to Marker.NONE, the marker is null
                    g.setColor(series.getMarkerColor());
                    series.getMarker().paint(g, xOffset, yOffset, stylerXY.getMarkerSize());
                }

                // paint error bars
                if (errorBars != null) {

                    double eb = ebItr.next().doubleValue();

                    // set error bar style
                    if (stylerXY.isErrorBarsColorSeriesColor()) {
                        g.setColor(series.getLineColor());
                    } else {
                        g.setColor(stylerXY.getErrorBarsColor());
                    }
                    g.setStroke(errorBarStroke);

                    // Top value
                    double topValue = 0.0;
                    if (stylerXY.isYAxisLogarithmic()) {
                        topValue = yOrig + eb;
                        topValue = Math.log10(topValue);
                    } else {
                        topValue = y + eb;
                    }
                    double topEBTransform = bounds.getHeight() - (yTopMargin + (topValue - yMin) / (yMax - yMin) * yTickSpace);
                    double topEBOffset = bounds.getY() + topEBTransform;

                    // Bottom value
                    double bottomValue = 0.0;
                    if (stylerXY.isYAxisLogarithmic()) {
                        bottomValue = yOrig - eb;
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

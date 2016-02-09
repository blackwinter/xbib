package org.xbib.graphics.chart.internal.component;

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

public class PlotContentCategoryBar<ST extends Styler, S extends Series> extends PlotContent {

    StylerCategory stylerCategory;

    /**
     * Constructor
     *
     * @param chart
     */
    protected PlotContentCategoryBar(Chart<StylerCategory, SeriesCategory> chart) {

        super(chart);
        this.stylerCategory = chart.getStyler();
    }

    @Override
    public void paint(Graphics2D g) {

        Rectangle2D bounds = getBounds();
        // g.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
        // g.setColor(Color.red);
        // g.draw(bounds);

        // this is for preventing the series to be drawn outside the plot area if min and max is overridden to fall inside the data range
        Rectangle2D rectangle = new Rectangle2D.Double(0, 0, chart.getWidth(), chart.getHeight());
        // g.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
        // g.setColor(Color.green);
        // g.draw(rectangle);
        g.setClip(bounds.createIntersection(rectangle));

        // X-Axis
        double xTickSpace = stylerCategory.getPlotContentSize() * bounds.getWidth();
        double xLeftMargin = Utils.getTickStartOffset(bounds.getWidth(), xTickSpace);

        // Y-Axis
        double yTickSpace = stylerCategory.getPlotContentSize() * bounds.getHeight();
        double yTopMargin = Utils.getTickStartOffset(bounds.getHeight(), yTickSpace);

        Map<String, SeriesCategory> seriesMap = chart.getSeriesMap();
        int numCategories = seriesMap.values().iterator().next().getXData().size();
        double gridStep = xTickSpace / numCategories;

        double yMin = chart.getAxisPair().getYAxis().getMin();
        double yMax = chart.getAxisPair().getYAxis().getMax();

        // figure out the general form of the chart
        int chartForm = 1; // 1=positive, -1=negative, 0=span
        if (yMin > 0.0 && yMax > 0.0) {
            chartForm = 1; // positive chart
        } else if (yMin < 0.0 && yMax < 0.0) {
            chartForm = -1; // negative chart
        } else {
            chartForm = 0;// span chart
        }
        // plot series
        int seriesCounter = 0;
        for (SeriesCategory series : seriesMap.values()) {

            // for line series
            double previousX = -Double.MAX_VALUE;
            double previousY = -Double.MAX_VALUE;

            Iterator<? extends Number> yItr = series.getYData().iterator();
            Iterator<? extends Number> ebItr = null;
            Collection<? extends Number> errorBars = series.getErrorBars();
            if (errorBars != null) {
                ebItr = errorBars.iterator();
            }

            int categoryCounter = 0;
            while (yItr.hasNext()) {

                Number next = yItr.next();
                if (next == null) {

                    previousX = -Double.MAX_VALUE;
                    previousY = -Double.MAX_VALUE;
                    categoryCounter++;
                    continue;
                }
                double y = next.doubleValue();

                double yTop = 0.0;
                double yBottom = 0.0;
                switch (chartForm) {
                    case 1: // positive chart
                        // check for points off the chart draw area due to a custom yMin
                        if (y < yMin) {
                            categoryCounter++;
                            continue;
                        }
                        yTop = y;
                        yBottom = yMin;
                        break;
                    case -1: // negative chart
                        // check for points off the chart draw area due to a custom yMin
                        if (y > yMax) {
                            categoryCounter++;
                            continue;
                        }
                        yTop = yMax;
                        yBottom = y;
                        break;
                    case 0: // span chart
                        if (y >= 0.0) { // positive
                            yTop = y;
                            yBottom = 0.0;
                        } else {
                            yTop = 0.0;
                            yBottom = y;
                        }
                        break;
                    default:
                        break;
                }

                double yTransform = bounds.getHeight() - (yTopMargin + (yTop - yMin) / (yMax - yMin) * yTickSpace);
                // double yTransform = bounds.getHeight() - (yTopMargin + (y - yMin) / (yMax - yMin) * yTickSpace);

                double yOffset = bounds.getY() + yTransform;

                double zeroTransform = bounds.getHeight() - (yTopMargin + (yBottom - yMin) / (yMax - yMin) * yTickSpace);
                double zeroOffset = bounds.getY() + zeroTransform;
                double xOffset;
                double barWidth;

                if (stylerCategory.isBarsOverlapped()) {
                    double barWidthPercentage = stylerCategory.getBarWidthPercentage();
                    barWidth = gridStep * barWidthPercentage;
                    double barMargin = gridStep * (1 - barWidthPercentage) / 2;
                    if (ChartCategorySeriesRenderStyle.Stick.equals(series.getChartCategorySeriesRenderStyle())) {
                        xOffset = bounds.getX() + xLeftMargin + categoryCounter++ * gridStep + gridStep / 2;
                    } else {
                        xOffset = bounds.getX() + xLeftMargin + gridStep * categoryCounter++ + barMargin;
                    }
                } else {
                    double barWidthPercentage = stylerCategory.getBarWidthPercentage();
                    barWidth = gridStep / chart.getSeriesMap().size() * barWidthPercentage;
                    double barMargin = gridStep * (1 - barWidthPercentage) / 2;
                    if (ChartCategorySeriesRenderStyle.Stick.equals(series.getChartCategorySeriesRenderStyle())) {
                        xOffset = bounds.getX() + xLeftMargin + categoryCounter++ * gridStep + seriesCounter * barMargin + gridStep / chart.getSeriesMap().size() / 2;
                    } else {
                        xOffset = bounds.getX() + xLeftMargin + gridStep * categoryCounter++ + seriesCounter * barWidth + barMargin;
                    }
                }

                // paint series
                if (series.getChartCategorySeriesRenderStyle() == ChartCategorySeriesRenderStyle.Bar) {

                    // paint bar
                    Path2D.Double path = new Path2D.Double();
                    path.moveTo(xOffset, yOffset);
                    path.lineTo(xOffset + barWidth, yOffset);
                    path.lineTo(xOffset + barWidth, zeroOffset);
                    path.lineTo(xOffset, zeroOffset);
                    path.closePath();

                    // g.setStroke(series.getLineStyle());
                    // g.setColor(series.getLineColor());
                    // g.draw(path);
                    g.setColor(series.getFillColor());
                    g.fill(path);
                } else if (ChartCategorySeriesRenderStyle.Stick.equals(series.getChartCategorySeriesRenderStyle())) {

                    // paint line
                    if (series.getLineStyle() != SeriesLines.NONE) {

                        g.setColor(series.getLineColor());
                        g.setStroke(series.getLineStyle());
                        Shape line = new Line2D.Double(xOffset, zeroOffset, xOffset, yOffset);
                        g.draw(line);
                    }

                    // paint marker
                    if (series.getMarker() != null) {
                        g.setColor(series.getMarkerColor());

                        if (y <= 0) {
                            series.getMarker().paint(g, xOffset, zeroOffset, stylerCategory.getMarkerSize());
                        } else {
                            series.getMarker().paint(g, xOffset, yOffset, stylerCategory.getMarkerSize());
                        }
                    }
                } else {

                    // paint line
                    if (series.getChartCategorySeriesRenderStyle() == ChartCategorySeriesRenderStyle.Line) {

                        if (series.getLineStyle() != SeriesLines.NONE) {

                            if (previousX != -Double.MAX_VALUE && previousY != -Double.MAX_VALUE) {
                                g.setColor(series.getLineColor());
                                g.setStroke(series.getLineStyle());
                                Shape line = new Line2D.Double(previousX, previousY, xOffset + barWidth / 2, yOffset);
                                g.draw(line);
                            }
                        }
                    }
                    previousX = xOffset + barWidth / 2;
                    previousY = yOffset;

                    // paint marker
                    if (series.getMarker() != null) {
                        g.setColor(series.getMarkerColor());
                        series.getMarker().paint(g, previousX, previousY, stylerCategory.getMarkerSize());
                    }

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
                    double topValue = y + eb;
                    double topEBTransform = bounds.getHeight() - (yTopMargin + (topValue - yMin) / (yMax - yMin) * yTickSpace);
                    double topEBOffset = bounds.getY() + topEBTransform;

                    // Bottom value
                    double bottomValue = y - eb;
                    double bottomEBTransform = bounds.getHeight() - (yTopMargin + (bottomValue - yMin) / (yMax - yMin) * yTickSpace);
                    double bottomEBOffset = bounds.getY() + bottomEBTransform;

                    // Draw it
                    double errorBarOffset = xOffset + barWidth / 2;
                    Shape line = new Line2D.Double(errorBarOffset, topEBOffset, errorBarOffset, bottomEBOffset);
                    g.draw(line);
                    line = new Line2D.Double(errorBarOffset - 3, bottomEBOffset, errorBarOffset + 3, bottomEBOffset);
                    g.draw(line);
                    line = new Line2D.Double(errorBarOffset - 3, topEBOffset, errorBarOffset + 3, topEBOffset);
                    g.draw(line);
                }

            }
            seriesCounter++;
        }
        g.setClip(null);
    }

}

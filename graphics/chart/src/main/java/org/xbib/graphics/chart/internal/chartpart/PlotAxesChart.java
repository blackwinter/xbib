package org.xbib.graphics.chart.internal.chartpart;

import org.xbib.graphics.chart.SeriesXY;
import org.xbib.graphics.chart.internal.Series;
import org.xbib.graphics.chart.internal.style.Styler;
import org.xbib.graphics.chart.internal.style.StylerAxesChart;

import java.awt.*;
import java.awt.geom.Rectangle2D;

public class PlotAxesChart<ST extends Styler, S extends Series> extends Plot {

    StylerAxesChart stylerAxesChart;

    /**
     * Constructor
     *
     * @param chart
     */
    public PlotAxesChart(Chart<StylerAxesChart, SeriesXY> chart) {

        super(chart);
        stylerAxesChart = chart.getStyler();
        this.plotSurface = new PlotSurfaceAxesChart<StylerAxesChart, SeriesXY>(chart);
    }

    @Override
    public void paint(Graphics2D g) {

        // calculate bounds
        double xOffset = chart.getYAxis().getBounds().getX() + chart.getYAxis().getBounds().getWidth()

                + (stylerAxesChart.isYAxisTicksVisible() ? stylerAxesChart.getPlotMargin() : 0);

        double yOffset = chart.getYAxis().getBounds().getY();
        double width = chart.getXAxis().getBounds().getWidth();
        double height = chart.getYAxis().getBounds().getHeight();
        this.bounds = new Rectangle2D.Double(xOffset, yOffset, width, height);

        super.paint(g);
    }
}

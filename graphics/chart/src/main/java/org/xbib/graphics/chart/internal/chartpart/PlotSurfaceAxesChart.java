package org.xbib.graphics.chart.internal.chartpart;

import org.xbib.graphics.chart.SeriesXY;
import org.xbib.graphics.chart.internal.Series;
import org.xbib.graphics.chart.internal.style.Styler;
import org.xbib.graphics.chart.internal.style.StylerAxesChart;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

/**
 * Draws the plot background, the plot border and the horizontal and vertical grid lines
 */
public class PlotSurfaceAxesChart<ST extends Styler, S extends Series> extends PlotSurface {

    private final StylerAxesChart stylerAxesChart;

    /**
     * Constructor
     *
     * @param chart
     */
    protected PlotSurfaceAxesChart(Chart<StylerAxesChart, SeriesXY> chart) {

        super(chart);
        this.stylerAxesChart = chart.getStyler();
    }

    @Override
    public void paint(Graphics2D g) {

        Rectangle2D bounds = getBounds();

        // paint plot background
        Shape rect = new Rectangle2D.Double(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
        g.setColor(stylerAxesChart.getPlotBackgroundColor());
        g.fill(rect);

        // paint plot border
        if (stylerAxesChart.isPlotBorderVisible()) {
            g.setColor(stylerAxesChart.getPlotBorderColor());
            // g.setStroke(getChartPainter().getstyler().getAxisTickMarksStroke());
            g.draw(rect);
        }

        // paint grid lines and/or inner plot ticks

        // horizontal
        if (stylerAxesChart.isPlotGridHorizontalLinesVisible() || stylerAxesChart.isPlotTicksMarksVisible()) {

            List<Double> yAxisTickLocations = chart.getYAxis().getAxisTickCalculator().getTickLocations();
            for (int i = 0; i < yAxisTickLocations.size(); i++) {

                double yOffset = bounds.getY() + bounds.getHeight() - yAxisTickLocations.get(i);

                if (yOffset > bounds.getY() && yOffset < bounds.getY() + bounds.getHeight()) {

                    // draw lines
                    if (stylerAxesChart.isPlotGridHorizontalLinesVisible()) {

                        g.setColor(stylerAxesChart.getPlotGridLinesColor());
                        g.setStroke(stylerAxesChart.getPlotGridLinesStroke());
                        Shape line = new Line2D.Double(bounds.getX(), yOffset, bounds.getX() + bounds.getWidth(), yOffset);
                        g.draw(line);
                    }

                    // tick marks
                    if (stylerAxesChart.isPlotTicksMarksVisible()) {

                        g.setColor(stylerAxesChart.getAxisTickMarksColor());
                        g.setStroke(stylerAxesChart.getAxisTickMarksStroke());
                        Shape line = new Line2D.Double(bounds.getX(), yOffset, bounds.getX() + stylerAxesChart.getAxisTickMarkLength(), yOffset);
                        g.draw(line);
                        line = new Line2D.Double(bounds.getX() + bounds.getWidth(), yOffset, bounds.getX() + bounds.getWidth() - stylerAxesChart.getAxisTickMarkLength(), yOffset);
                        g.draw(line);
                    }
                }
            }
        }

        // vertical

        if ((stylerAxesChart.isPlotGridVerticalLinesVisible() || stylerAxesChart.isPlotTicksMarksVisible())) {

            List<Double> xAxisTickLocations = chart.getXAxis().getAxisTickCalculator().getTickLocations();
            for (int i = 0; i < xAxisTickLocations.size(); i++) {

                double tickLocation = xAxisTickLocations.get(i);
                double xOffset = bounds.getX() + tickLocation;

                if (xOffset > bounds.getX() && xOffset < bounds.getX() + bounds.getWidth()) {

                    // draw lines
                    if (stylerAxesChart.isPlotGridVerticalLinesVisible()) {
                        g.setColor(stylerAxesChart.getPlotGridLinesColor());
                        g.setStroke(stylerAxesChart.getPlotGridLinesStroke());

                        Shape line = new Line2D.Double(xOffset, bounds.getY(), xOffset, bounds.getY() + bounds.getHeight());
                        g.draw(line);
                    }
                    // tick marks
                    if (stylerAxesChart.isPlotTicksMarksVisible()) {

                        g.setColor(stylerAxesChart.getAxisTickMarksColor());
                        g.setStroke(stylerAxesChart.getAxisTickMarksStroke());

                        Shape line = new Line2D.Double(xOffset, bounds.getY(), xOffset, bounds.getY() + stylerAxesChart.getAxisTickMarkLength());
                        g.draw(line);
                        line = new Line2D.Double(xOffset, bounds.getY() + bounds.getHeight(), xOffset, bounds.getY() + bounds.getHeight() - stylerAxesChart.getAxisTickMarkLength());
                        g.draw(line);
                    }
                }
            }
        }
    }

}

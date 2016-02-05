package org.xbib.graphics.chart.internal.chartpart;

import org.xbib.graphics.chart.internal.Series;
import org.xbib.graphics.chart.internal.SeriesAxesChart;
import org.xbib.graphics.chart.internal.chartpart.Axis.Direction;
import org.xbib.graphics.chart.internal.style.StylerAxesChart;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

/**
 * Axis tick marks. This includes the little tick marks and the line that hugs the plot area.
 */
public class AxisTickMarks<ST extends StylerAxesChart, S extends Series> implements ChartPart {

    private final Chart<StylerAxesChart, SeriesAxesChart> chart;
    private final Direction direction;
    private Rectangle2D bounds;

    /**
     * Constructor
     *
     * @param chart
     * @param direction
     */
    protected AxisTickMarks(Chart<StylerAxesChart, SeriesAxesChart> chart, Direction direction) {

        this.chart = chart;
        this.direction = direction;
    }

    @Override
    public void paint(Graphics2D g) {

        g.setColor(chart.getStyler().getAxisTickMarksColor());
        g.setStroke(chart.getStyler().getAxisTickMarksStroke());

        if (direction == Axis.Direction.Y && chart.getStyler().isYAxisTicksVisible()) { // Y-Axis

            double xOffset = chart.getYAxis().getAxisTick().getAxisTickLabels().getBounds().getX() + chart.getYAxis().getAxisTick().getAxisTickLabels().getBounds().getWidth() + chart.getStyler()
                    .getAxisTickPadding();
            double yOffset = chart.getYAxis().getPaintZone().getY();

            // bounds
            bounds = new Rectangle2D.Double(xOffset, yOffset, chart.getStyler().getAxisTickMarkLength(), chart.getYAxis().getPaintZone().getHeight());

            // tick marks
            if (chart.getStyler().isAxisTicksMarksVisible()) {

                for (int i = 0; i < chart.getYAxis().getAxisTickCalculator().getTickLabels().size(); i++) {

                    double tickLocation = chart.getYAxis().getAxisTickCalculator().getTickLocations().get(i);
                    double flippedTickLocation = yOffset + chart.getYAxis().getPaintZone().getHeight() - tickLocation;
                    if (flippedTickLocation > bounds.getY() && flippedTickLocation < bounds.getY() + bounds.getHeight()) {

                        Shape line = new Line2D.Double(xOffset, flippedTickLocation, xOffset + chart.getStyler().getAxisTickMarkLength(), flippedTickLocation);
                        g.draw(line);
                    }
                }
            }

            // Line
            if (chart.getStyler().isAxisTicksLineVisible()) {

                Shape line = new Line2D.Double(xOffset + chart.getStyler().getAxisTickMarkLength(), yOffset, xOffset + chart.getStyler().getAxisTickMarkLength(), yOffset + chart.getYAxis().getPaintZone()
                        .getHeight());
                g.draw(line);

            }

        }
        // X-Axis
        else if (direction == Axis.Direction.X && chart.getStyler().isXAxisTicksVisible()) {

            double xOffset = chart.getXAxis().getPaintZone().getX();
            double yOffset = chart.getXAxis().getAxisTick().getAxisTickLabels().getBounds().getY() - chart.getStyler().getAxisTickPadding();

            // bounds
            bounds = new Rectangle2D.Double(xOffset, yOffset - chart.getStyler().getAxisTickMarkLength(), chart.getXAxis().getPaintZone().getWidth(), chart.getStyler().getAxisTickMarkLength());

            // tick marks
            if (chart.getStyler().isAxisTicksMarksVisible()) {

                for (int i = 0; i < chart.getXAxis().getAxisTickCalculator().getTickLabels().size(); i++) {

                    double tickLocation = chart.getXAxis().getAxisTickCalculator().getTickLocations().get(i);
                    double shiftedTickLocation = xOffset + tickLocation;

                    if (shiftedTickLocation > bounds.getX() && shiftedTickLocation < bounds.getX() + bounds.getWidth()) {

                        Shape line = new Line2D.Double(shiftedTickLocation, yOffset, xOffset + tickLocation, yOffset - chart.getStyler().getAxisTickMarkLength());
                        g.draw(line);
                    }
                }
            }

            // Line
            if (chart.getStyler().isAxisTicksLineVisible()) {

                g.setStroke(chart.getStyler().getAxisTickMarksStroke());
                g.drawLine((int) xOffset, (int) (yOffset - chart.getStyler().getAxisTickMarkLength()), (int) (xOffset + chart.getXAxis().getPaintZone().getWidth()), (int) (yOffset - chart.getStyler()
                        .getAxisTickMarkLength()));
            }

        } else {
            bounds = new Rectangle2D.Double();
        }

    }

    @Override
    public Rectangle2D getBounds() {

        return bounds;
    }
}

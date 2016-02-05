package org.xbib.graphics.chart.internal.chartpart;

import org.xbib.graphics.chart.internal.Series;
import org.xbib.graphics.chart.internal.SeriesAxesChart;
import org.xbib.graphics.chart.internal.chartpart.Axis.Direction;
import org.xbib.graphics.chart.internal.style.StylerAxesChart;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;

/**
 * Axis tick labels
 */
public class AxisTickLabels<ST extends StylerAxesChart, S extends Series> implements ChartPart {

    private final Chart<StylerAxesChart, SeriesAxesChart> chart;
    private final Direction direction;
    private Rectangle2D bounds;

    /**
     * Constructor
     *
     * @param chart
     * @param direction
     */
    protected AxisTickLabels(Chart<StylerAxesChart, SeriesAxesChart> chart, Direction direction) {

        this.chart = chart;
        this.direction = direction;
    }

    @Override
    public void paint(Graphics2D g) {

        g.setFont(chart.getStyler().getAxisTickLabelsFont());

        g.setColor(chart.getStyler().getAxisTickLabelsColor());

        if (direction == Axis.Direction.Y && chart.getStyler().isYAxisTicksVisible()) { // Y-Axis

            double xWidth = chart.getYAxis().getAxisTitle().getBounds().getWidth();
            double xOffset = chart.getYAxis().getAxisTitle().getBounds().getX() + xWidth;
            double yOffset = chart.getYAxis().getPaintZone().getY();
            double height = chart.getYAxis().getPaintZone().getHeight();
            double maxTickLabelWidth = 0;
            Map<Double, TextLayout> axisLabelTextLayouts = new HashMap<Double, TextLayout>();

            for (int i = 0; i < chart.getYAxis().getAxisTickCalculator().getTickLabels().size(); i++) {

                String tickLabel = chart.getYAxis().getAxisTickCalculator().getTickLabels().get(i);
                // System.out.println("** " + tickLabel);
                double tickLocation = chart.getYAxis().getAxisTickCalculator().getTickLocations().get(i);
                double flippedTickLocation = yOffset + height - tickLocation;

                if (tickLabel != null && flippedTickLocation > yOffset && flippedTickLocation < yOffset + height) { // some are null for logarithmic axes
                    FontRenderContext frc = g.getFontRenderContext();
                    TextLayout axisLabelTextLayout = new TextLayout(tickLabel, chart.getStyler().getAxisTickLabelsFont(), frc);
                    Rectangle2D tickLabelBounds = axisLabelTextLayout.getBounds();
                    double boundWidth = tickLabelBounds.getWidth();
                    if (boundWidth > maxTickLabelWidth) {
                        maxTickLabelWidth = boundWidth;
                    }
                    axisLabelTextLayouts.put(tickLocation, axisLabelTextLayout);
                }
            }

            for (Double tickLocation : axisLabelTextLayouts.keySet()) {

                TextLayout axisLabelTextLayout = axisLabelTextLayouts.get(tickLocation);
                Shape shape = axisLabelTextLayout.getOutline(null);
                Rectangle2D tickLabelBounds = shape.getBounds();

                double flippedTickLocation = yOffset + height - tickLocation;

                AffineTransform orig = g.getTransform();
                AffineTransform at = new AffineTransform();
                double boundWidth = tickLabelBounds.getWidth();
                double xPos;
                switch (chart.getStyler().getYAxisLabelAlignment()) {
                    case Right:
                        xPos = xOffset + maxTickLabelWidth - boundWidth;
                        break;
                    case Centre:
                        xPos = xOffset + (maxTickLabelWidth - boundWidth) / 2;
                        break;
                    case Left:
                    default:
                        xPos = xOffset;
                }
                at.translate(xPos, flippedTickLocation + tickLabelBounds.getHeight() / 2.0);
                g.transform(at);
                g.fill(shape);
                g.setTransform(orig);

            }

            // bounds
            bounds = new Rectangle2D.Double(xOffset, yOffset, maxTickLabelWidth, height);
            // g.setColor(Color.blue);
            // g.draw(bounds);

        }
        // X-Axis
        else if (direction == Axis.Direction.X && chart.getStyler().isXAxisTicksVisible()) {

            double xOffset = chart.getXAxis().getPaintZone().getX();
            double yOffset = chart.getXAxis().getAxisTitle().getBounds().getY();
            double width = chart.getXAxis().getPaintZone().getWidth();
            double maxTickLabelHeight = 0;

            // System.out.println("axisTick.getTickLabels().size(): " + axisTick.getTickLabels().size());
            for (int i = 0; i < chart.getXAxis().getAxisTickCalculator().getTickLabels().size(); i++) {

                String tickLabel = chart.getXAxis().getAxisTickCalculator().getTickLabels().get(i);
                // System.out.println("tickLabel: " + tickLabel);
                double tickLocation = chart.getXAxis().getAxisTickCalculator().getTickLocations().get(i);
                double shiftedTickLocation = xOffset + tickLocation;

                // discard null and out of bounds labels
                if (tickLabel != null && shiftedTickLocation > xOffset && shiftedTickLocation < xOffset + width) { // some are null for logarithmic axes

                    FontRenderContext frc = g.getFontRenderContext();
                    TextLayout textLayout = new TextLayout(tickLabel, chart.getStyler().getAxisTickLabelsFont(), frc);
                    // System.out.println(textLayout.getOutline(null).getBounds().toString());

                    // Shape shape = v.getOutline();
                    AffineTransform rot = AffineTransform.getRotateInstance(-1 * Math.toRadians(chart.getStyler().getXAxisLabelRotation()), 0, 0);
                    Shape shape = textLayout.getOutline(rot);
                    Rectangle2D tickLabelBounds = shape.getBounds2D();

                    AffineTransform orig = g.getTransform();
                    AffineTransform at = new AffineTransform();
                    double xPos;
                    switch (chart.getStyler().getXAxisLabelAlignment()) {
                        case Left:
                            xPos = shiftedTickLocation;
                            break;
                        case Right:
                            xPos = shiftedTickLocation - tickLabelBounds.getWidth();
                            break;
                        case Centre:
                        default:
                            xPos = shiftedTickLocation - tickLabelBounds.getWidth() / 2.0;
                    }
                    double shiftX = -1 * tickLabelBounds.getX() * Math.sin(Math.toRadians(chart.getStyler().getXAxisLabelRotation()));
                    double shiftY = -1 * (tickLabelBounds.getY() + tickLabelBounds.getHeight());
                    at.translate(xPos + shiftX, yOffset + shiftY);

                    g.transform(at);
                    g.fill(shape);
                    g.setTransform(orig);

                    if (tickLabelBounds.getHeight() > maxTickLabelHeight) {
                        maxTickLabelHeight = tickLabelBounds.getHeight();
                    }
                }
            }

            // bounds
            bounds = new Rectangle2D.Double(xOffset, yOffset - maxTickLabelHeight, width, maxTickLabelHeight);

        } else {
            bounds = new Rectangle2D.Double();
        }

    }

    @Override
    public Rectangle2D getBounds() {

        return bounds;
    }
}

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

/**
 * AxisTitle
 */
public class AxisTitle<ST extends StylerAxesChart, S extends Series> implements ChartPart {

    private final Chart<StylerAxesChart, SeriesAxesChart> chart;
    private final Direction direction;
    private Rectangle2D bounds;

    /**
     * Constructor
     *
     * @param chart     the Chart
     * @param direction the Direction
     */
    protected AxisTitle(Chart<StylerAxesChart, SeriesAxesChart> chart, Direction direction) {

        this.chart = chart;
        this.direction = direction;
    }

    @Override
    public void paint(Graphics2D g) {

        bounds = new Rectangle2D.Double();

        g.setColor(chart.getStyler().getChartFontColor());
        g.setFont(chart.getStyler().getAxisTitleFont());

        if (direction == Axis.Direction.Y) {

            if (chart.getyYAxisTitle() != null && !chart.getyYAxisTitle().trim().equalsIgnoreCase("") && chart.getStyler().isYAxisTitleVisible()) {

                FontRenderContext frc = g.getFontRenderContext();
                TextLayout nonRotatedTextLayout = new TextLayout(chart.getyYAxisTitle(), chart.getStyler().getAxisTitleFont(), frc);
                Rectangle2D nonRotatedRectangle = nonRotatedTextLayout.getBounds();

                int xOffset = (int) (chart.getYAxis().getPaintZone().getX() + nonRotatedRectangle.getHeight());
                int yOffset = (int) ((chart.getYAxis().getPaintZone().getHeight() + nonRotatedRectangle.getWidth()) / 2.0 + chart.getYAxis().getPaintZone().getY());

                AffineTransform rot = AffineTransform.getRotateInstance(-1 * Math.PI / 2, 0, 0);
                Shape shape = nonRotatedTextLayout.getOutline(rot);

                AffineTransform orig = g.getTransform();
                AffineTransform at = new AffineTransform();

                at.translate(xOffset, yOffset);
                g.transform(at);
                g.fill(shape);
                g.setTransform(orig);

                // bounds
                bounds = new Rectangle2D.Double(xOffset - nonRotatedRectangle.getHeight(), yOffset - nonRotatedRectangle.getWidth(), nonRotatedRectangle.getHeight() + chart.getStyler().getAxisTitlePadding(),
                        nonRotatedRectangle.getWidth());
            } else {
                bounds = new Rectangle2D.Double(chart.getYAxis().getPaintZone().getX(), chart.getYAxis().getPaintZone().getY(), 0, chart.getYAxis().getPaintZone().getHeight());
            }

        } else {

            if (chart.getXAxisTitle() != null && !chart.getXAxisTitle().trim().equalsIgnoreCase("") && chart.getStyler().isXAxisTitleVisible()) {

                FontRenderContext frc = g.getFontRenderContext();
                TextLayout textLayout = new TextLayout(chart.getXAxisTitle(), chart.getStyler().getAxisTitleFont(), frc);
                Rectangle2D rectangle = textLayout.getBounds();

                double xOffset = chart.getXAxis().getPaintZone().getX() + (chart.getXAxis().getPaintZone().getWidth() - rectangle.getWidth()) / 2.0;
                double yOffset = chart.getXAxis().getPaintZone().getY() + chart.getXAxis().getPaintZone().getHeight() - rectangle.getHeight();

                // textLayout.draw(g, (float) xOffset, (float) (yOffset - rectangle.getY()));
                Shape shape = textLayout.getOutline(null);
                AffineTransform orig = g.getTransform();
                AffineTransform at = new AffineTransform();
                at.translate((float) xOffset, (float) (yOffset - rectangle.getY()));
                g.transform(at);
                g.fill(shape);
                g.setTransform(orig);

                bounds = new Rectangle2D.Double(xOffset, yOffset - chart.getStyler().getAxisTitlePadding(), rectangle.getWidth(), rectangle.getHeight() + chart.getStyler().getAxisTitlePadding());
                // g.setColor(Color.blue);
                // g.draw(bounds);

            } else {
                bounds = new Rectangle2D.Double(chart.getXAxis().getPaintZone().getX(), chart.getXAxis().getPaintZone().getY() + chart.getXAxis().getPaintZone().getHeight(), chart.getXAxis().getPaintZone()
                        .getWidth(), 0);

            }
        }
    }

    @Override
    public Rectangle2D getBounds() {

        return bounds;
    }
}

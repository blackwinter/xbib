package org.xbib.graphics.chart.internal.component;

import org.xbib.graphics.chart.internal.Series;
import org.xbib.graphics.chart.internal.SeriesAxesChart;
import org.xbib.graphics.chart.internal.component.Axis.Direction;
import org.xbib.graphics.chart.internal.style.StylerAxesChart;

import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * An axis tick
 */
public class AxisTick<ST extends StylerAxesChart, S extends Series> implements ChartPart {

    private final Chart<StylerAxesChart, SeriesAxesChart> chart;
    private final Direction direction;
    private Rectangle2D bounds;
    /**
     * the axisticklabels
     */
    private AxisTickLabels<StylerAxesChart, SeriesAxesChart> axisTickLabels;

    /**
     * the axistickmarks
     */
    private AxisTickMarks<StylerAxesChart, SeriesAxesChart> axisTickMarks;

    /**
     * Constructor
     *
     * @param chart
     * @param direction
     */
    protected AxisTick(Chart<StylerAxesChart, SeriesAxesChart> chart, Direction direction) {

        this.chart = chart;
        this.direction = direction;
        axisTickLabels = new AxisTickLabels<StylerAxesChart, SeriesAxesChart>(chart, direction);
        axisTickMarks = new AxisTickMarks<StylerAxesChart, SeriesAxesChart>(chart, direction);
    }

    @Override
    public Rectangle2D getBounds() {

        return bounds;
    }

    @Override
    public void paint(Graphics2D g) {

        if (direction == Axis.Direction.Y && chart.getStyler().isYAxisTicksVisible()) {

            axisTickLabels.paint(g);
            axisTickMarks.paint(g);

            bounds = new Rectangle2D.Double(

                    axisTickLabels.getBounds().getX(),

                    axisTickLabels.getBounds().getY(),

                    axisTickLabels.getBounds().getWidth() + chart.getStyler().getAxisTickPadding() + axisTickMarks.getBounds().getWidth(),

                    axisTickMarks.getBounds().getHeight()

            );

            // g.setColor(Color.red);
            // g.draw(bounds);

        } else if (direction == Axis.Direction.X && chart.getStyler().isXAxisTicksVisible()) {

            axisTickLabels.paint(g);
            axisTickMarks.paint(g);

            bounds = new Rectangle2D.Double(

                    axisTickMarks.getBounds().getX(),

                    axisTickMarks.getBounds().getY(),

                    axisTickLabels.getBounds().getWidth(),

                    axisTickMarks.getBounds().getHeight() + chart.getStyler().getAxisTickPadding() + axisTickLabels.getBounds().getHeight()

            );

            // g.setColor(Color.red);
            // g.draw(bounds);

        } else {
            bounds = new Rectangle2D.Double();
        }

    }

    // Getters /////////////////////////////////////////////////

    protected AxisTickLabels<StylerAxesChart, SeriesAxesChart> getAxisTickLabels() {

        return axisTickLabels;
    }

}

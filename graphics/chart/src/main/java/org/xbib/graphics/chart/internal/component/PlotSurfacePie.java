package org.xbib.graphics.chart.internal.component;

import org.xbib.graphics.chart.SeriesPie;
import org.xbib.graphics.chart.StylerPie;
import org.xbib.graphics.chart.internal.Series;
import org.xbib.graphics.chart.internal.style.Styler;

import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * Draws the plot background and the plot border
 */
public class PlotSurfacePie<ST extends Styler, S extends Series> extends PlotSurface {

    private final StylerPie stylerPie;

    /**
     * Constructor
     *
     * @param chart
     */
    protected PlotSurfacePie(Chart<StylerPie, SeriesPie> chart) {

        super(chart);
        this.stylerPie = chart.getStyler();
    }

    @Override
    public void paint(Graphics2D g) {

        Rectangle2D bounds = getBounds();

        // paint plot background
        Shape rect = new Rectangle2D.Double(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
        g.setColor(stylerPie.getPlotBackgroundColor());
        g.fill(rect);

        // paint plot border
        if (stylerPie.isPlotBorderVisible()) {
            g.setColor(stylerPie.getPlotBorderColor());
            g.draw(rect);
        }

    }

}

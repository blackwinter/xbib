package org.xbib.graphics.chart.internal.component;

import org.xbib.graphics.chart.internal.Series;
import org.xbib.graphics.chart.internal.style.Styler;

import java.awt.*;
import java.awt.geom.Rectangle2D;

public class Plot<ST extends Styler, S extends Series> implements ChartPart {

    protected final Chart<ST, S> chart;
    protected Rectangle2D bounds;

    protected PlotSurface plotSurface;
    protected PlotContent plotContent;

    /**
     * Constructor
     *
     * @param chart
     */
    public Plot(Chart<ST, S> chart) {

        this.chart = chart;
    }

    @Override
    public void paint(Graphics2D g) {

        plotSurface.paint(g);
        if (chart.getSeriesMap().isEmpty()) {
            return;
        }
        plotContent.paint(g);

    }

    @Override
    public Rectangle2D getBounds() {

        return bounds;
    }
}

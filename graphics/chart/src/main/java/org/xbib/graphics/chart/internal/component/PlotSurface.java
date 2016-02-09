package org.xbib.graphics.chart.internal.component;

import org.xbib.graphics.chart.internal.Series;
import org.xbib.graphics.chart.internal.style.Styler;

import java.awt.geom.Rectangle2D;

public abstract class PlotSurface<ST extends Styler, S extends Series> implements ChartPart {

    protected final Chart<ST, S> chart;

    /**
     * Constructor
     *
     * @param chart
     */
    protected PlotSurface(Chart<ST, S> chart) {

        this.chart = chart;
    }

    @Override
    public Rectangle2D getBounds() {

        return chart.getPlot().getBounds();
    }
}

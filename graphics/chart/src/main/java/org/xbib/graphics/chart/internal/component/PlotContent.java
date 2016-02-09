package org.xbib.graphics.chart.internal.component;

import org.xbib.graphics.chart.internal.Series;
import org.xbib.graphics.chart.internal.style.StylerAxesChart;

import java.awt.*;
import java.awt.geom.Rectangle2D;

public abstract class PlotContent<ST extends StylerAxesChart, S extends Series> implements ChartPart {

    protected final Chart<ST, S> chart;

    protected final Stroke errorBarStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);

    /**
     * Constructor
     *
     * @param chart - The Chart
     */
    protected PlotContent(Chart<ST, S> chart) {

        this.chart = chart;
    }

    @Override
    public Rectangle2D getBounds() {

        return chart.getPlot().getBounds();
    }

}

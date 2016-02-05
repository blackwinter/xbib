package org.xbib.graphics.chart.internal;

import org.xbib.graphics.chart.internal.chartpart.RenderableSeries.LegendRenderType;

import java.awt.*;

/**
 * A Series containing data to be plotted on a Chart
 */
public abstract class Series {

    private final String name;
    private Color fillColor;
    private boolean showInLegend = true;

    /**
     * Constructor
     *
     * @param name
     */
    public Series(String name) {

        if (name == null || name.length() < 1) {
            throw new IllegalArgumentException("Series name cannot be null or zero-length!!!");
        }
        this.name = name;
    }

    public abstract LegendRenderType getLegendRenderType();

    public Color getFillColor() {

        return fillColor;
    }

    public void setFillColor(Color fillColor) {

        this.fillColor = fillColor;
    }

    public String getName() {

        return name;
    }

    public boolean isShowInLegend() {

        return showInLegend;
    }

    public void setShowInLegend(boolean showInLegend) {

        this.showInLegend = showInLegend;
    }

}

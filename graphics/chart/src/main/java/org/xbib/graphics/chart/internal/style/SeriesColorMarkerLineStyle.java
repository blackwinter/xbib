package org.xbib.graphics.chart.internal.style;

import org.xbib.graphics.chart.internal.style.markers.Marker;

import java.awt.*;

/**
 * A DTO to hold the Series' Color, Marker, and LineStyle
 */
public final class SeriesColorMarkerLineStyle {

    private final Color color;
    private final Marker marker;
    private final BasicStroke stroke;

    /**
     * Constructor
     *
     * @param color
     * @param marker
     * @param stroke
     */
    public SeriesColorMarkerLineStyle(Color color, Marker marker, BasicStroke stroke) {

        this.color = color;
        this.marker = marker;
        this.stroke = stroke;
    }

    public Color getColor() {

        return color;
    }

    public Marker getMarker() {

        return marker;
    }

    public BasicStroke getStroke() {

        return stroke;
    }

}

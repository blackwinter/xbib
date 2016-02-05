package org.xbib.graphics.chart.internal.style.markers;

public class XChartSeriesMarkers implements SeriesMarkers {

    private final Marker[] seriesMarkers;

    /**
     * Constructor
     */
    public XChartSeriesMarkers() {

        seriesMarkers = new Marker[]{CIRCLE, DIAMOND, SQUARE, TRIANGLE_DOWN, TRIANGLE_UP};
    }

    @Override
    public Marker[] getSeriesMarkers() {

        return seriesMarkers;
    }
}

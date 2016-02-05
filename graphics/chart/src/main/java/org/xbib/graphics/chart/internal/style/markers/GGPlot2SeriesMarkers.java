package org.xbib.graphics.chart.internal.style.markers;

public class GGPlot2SeriesMarkers implements SeriesMarkers {

    private final Marker[] seriesMarkers;

    /**
     * Constructor
     */
    public GGPlot2SeriesMarkers() {

        seriesMarkers = new Marker[]{CIRCLE, DIAMOND};
    }

    @Override
    public Marker[] getSeriesMarkers() {

        return seriesMarkers;
    }
}
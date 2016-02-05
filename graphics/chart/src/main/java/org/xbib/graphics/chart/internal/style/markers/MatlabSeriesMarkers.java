package org.xbib.graphics.chart.internal.style.markers;

public class MatlabSeriesMarkers implements SeriesMarkers {

    private final Marker[] seriesMarkers;

    /**
     * Constructor
     */
    public MatlabSeriesMarkers() {

        seriesMarkers = new Marker[]{NONE};
    }

    @Override
    public Marker[] getSeriesMarkers() {

        return seriesMarkers;
    }
}

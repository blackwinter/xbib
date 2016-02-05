package org.xbib.graphics.chart.internal.style.markers;

public interface SeriesMarkers {

    Marker NONE = new None();
    Marker CIRCLE = new Circle();
    Marker DIAMOND = new Diamond();
    Marker SQUARE = new Square();
    Marker TRIANGLE_DOWN = new TriangleDown();
    Marker TRIANGLE_UP = new TriangleUp();

    Marker[] getSeriesMarkers();

}

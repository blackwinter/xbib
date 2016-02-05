package org.xbib.graphics.chart.internal.style.lines;

import java.awt.*;

public class MatlabSeriesLines implements SeriesLines {

    private final BasicStroke[] seriesLines;

    /**
     * Constructor
     */
    public MatlabSeriesLines() {

        seriesLines = new BasicStroke[]{SOLID, DASH_DASH, DOT_DOT};
    }

    @Override
    public BasicStroke[] getSeriesLines() {

        return seriesLines;
    }
}

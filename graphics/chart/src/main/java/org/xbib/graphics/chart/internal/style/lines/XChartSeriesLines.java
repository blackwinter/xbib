package org.xbib.graphics.chart.internal.style.lines;

import java.awt.*;

public class XChartSeriesLines implements SeriesLines {

    private final BasicStroke[] seriesLines;

    /**
     * Constructor
     */
    public XChartSeriesLines() {

        seriesLines = new BasicStroke[]{SOLID, DASH_DOT, DASH_DASH, DOT_DOT};
    }

    @Override
    public BasicStroke[] getSeriesLines() {

        return seriesLines;
    }
}

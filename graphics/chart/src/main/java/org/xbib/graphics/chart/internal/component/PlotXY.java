package org.xbib.graphics.chart.internal.component;

import org.xbib.graphics.chart.SeriesXY;
import org.xbib.graphics.chart.StylerXY;
import org.xbib.graphics.chart.internal.Series;
import org.xbib.graphics.chart.internal.style.StylerAxesChart;

public class PlotXY<ST extends StylerAxesChart, S extends Series> extends PlotAxesChart {

    /**
     * Constructor
     *
     * @param chart
     */
    public PlotXY(Chart<StylerXY, SeriesXY> chart) {

        super(chart);
        this.plotContent = new PlotContentXY<StylerXY, SeriesXY>(chart);
    }

}

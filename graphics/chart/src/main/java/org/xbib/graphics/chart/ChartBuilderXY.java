package org.xbib.graphics.chart;

import org.xbib.graphics.chart.internal.ChartBuilder;

public class ChartBuilderXY extends ChartBuilder<ChartBuilderXY, ChartXY> {

    String xAxisTitle = "";
    String yAxisTitle = "";

    public ChartBuilderXY() {

    }

    public ChartBuilderXY xAxisTitle(String xAxisTitle) {

        this.xAxisTitle = xAxisTitle;
        return this;
    }

    public ChartBuilderXY yAxisTitle(String yAxisTitle) {

        this.yAxisTitle = yAxisTitle;
        return this;
    }

    /**
     * return fully built ChartXY
     *
     * @return a ChartXY
     */
    @Override
    public ChartXY build() {

        return new ChartXY(this);
    }
}

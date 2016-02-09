package org.xbib.graphics.chart;

import org.xbib.graphics.chart.internal.ChartBuilder;

public class ChartBuilderXY extends ChartBuilder<ChartBuilderXY, ChartXY> {

    private String xAxisTitle = "";
    private String yAxisTitle = "";

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

    public String getxAxisTitle() {
        return xAxisTitle;
    }

    public String getyAxisTitle() {
        return yAxisTitle;
    }

    @Override
    public ChartXY build() {
        return new ChartXY(this);
    }
}

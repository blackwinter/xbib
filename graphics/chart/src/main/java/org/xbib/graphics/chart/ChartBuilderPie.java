package org.xbib.graphics.chart;

import org.xbib.graphics.chart.internal.ChartBuilder;

public class ChartBuilderPie extends ChartBuilder<ChartBuilderPie, ChartPie> {

    public ChartBuilderPie() {

    }

    /**
     * return fully built ChartPie
     *
     * @return a ChartPie
     */
    @Override
    public ChartPie build() {

        return new ChartPie(this);
    }
}

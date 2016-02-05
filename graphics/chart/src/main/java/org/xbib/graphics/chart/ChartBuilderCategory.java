package org.xbib.graphics.chart;

import org.xbib.graphics.chart.internal.ChartBuilder;

public class ChartBuilderCategory extends ChartBuilder<ChartBuilderCategory, ChartCategory> {

    String xAxisTitle = "";
    String yAxisTitle = "";

    public ChartBuilderCategory() {

    }

    public ChartBuilderCategory xAxisTitle(String xAxisTitle) {

        this.xAxisTitle = xAxisTitle;
        return this;
    }

    public ChartBuilderCategory yAxisTitle(String yAxisTitle) {

        this.yAxisTitle = yAxisTitle;
        return this;
    }

    /**
     * return fully built Chart_Category
     *
     * @return a Chart_Category
     */
    @Override
    public ChartCategory build() {

        return new ChartCategory(this);
    }
}

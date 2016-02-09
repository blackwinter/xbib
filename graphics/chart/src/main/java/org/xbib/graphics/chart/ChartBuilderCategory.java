package org.xbib.graphics.chart;

import org.xbib.graphics.chart.internal.ChartBuilder;

public class ChartBuilderCategory extends ChartBuilder<ChartBuilderCategory, ChartCategory> {

    private String xAxisTitle = "";
    private String yAxisTitle = "";

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

    public String getxAxisTitle() {
        return xAxisTitle;
    }

    public String getyAxisTitle() {
        return yAxisTitle;
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

package org.xbib.graphics.chart;

import org.xbib.graphics.chart.internal.SeriesAxesChart;
import org.xbib.graphics.chart.internal.chartpart.Axis.AxisDataType;
import org.xbib.graphics.chart.internal.chartpart.RenderableSeries;
import org.xbib.graphics.chart.internal.chartpart.RenderableSeries.LegendRenderType;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * A Series containing category data to be plotted on a Chart
 */
public class SeriesCategory extends SeriesAxesChart {

    private ChartCategorySeriesRenderStyle chartCategorySeriesRenderStyle = null;

    /**
     * Constructor
     *
     * @param name
     * @param xData
     * @param yData
     * @param errorBars
     */
    public SeriesCategory(String name, List<?> xData, List<? extends Number> yData, List<? extends Number> errorBars) {

        super(name, xData, yData, errorBars);

    }

    public ChartCategorySeriesRenderStyle getChartCategorySeriesRenderStyle() {

        return chartCategorySeriesRenderStyle;
    }

    public void setChartCategorySeriesRenderStyle(ChartCategorySeriesRenderStyle chartXYSeriesRenderStyle) {

        this.chartCategorySeriesRenderStyle = chartXYSeriesRenderStyle;
    }

    @Override
    public LegendRenderType getLegendRenderType() {

        return chartCategorySeriesRenderStyle.getLegendRenderType();
    }

    @Override
    public AxisDataType getAxesType(List<?> data) {

        AxisDataType axisType;

        Iterator<?> itr = data.iterator();
        Object dataPoint = itr.next();
        if (dataPoint instanceof Number) {
            axisType = AxisDataType.Number;
        } else if (dataPoint instanceof Date) {
            axisType = AxisDataType.Date;
        } else if (dataPoint instanceof String) {
            axisType = AxisDataType.String;
        } else {
            throw new IllegalArgumentException("Series data must be either Number, Date or String type!!!");
        }
        return axisType;
    }

    public enum ChartCategorySeriesRenderStyle implements RenderableSeries {

        Line(LegendRenderType.Line),

        Area(LegendRenderType.Line),

        Scatter(LegendRenderType.Scatter),

        Bar(LegendRenderType.Box),

        Stick(LegendRenderType.Line);

        private final LegendRenderType legendRenderType;

        private ChartCategorySeriesRenderStyle(LegendRenderType legendRenderType) {
            this.legendRenderType = legendRenderType;
        }

        @Override
        public LegendRenderType getLegendRenderType() {

            return legendRenderType;
        }
    }
}

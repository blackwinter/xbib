package org.xbib.graphics.chart;

import org.xbib.graphics.chart.internal.SeriesAxesChart;
import org.xbib.graphics.chart.internal.component.Axis.AxisDataType;
import org.xbib.graphics.chart.internal.component.RenderableSeries;
import org.xbib.graphics.chart.internal.component.RenderableSeries.LegendRenderType;

import java.time.Instant;
import java.util.Iterator;
import java.util.List;

/**
 * A Series containing X and Y data to be plotted on a Chart
 */
public class SeriesXY extends SeriesAxesChart {

    private ChartXYSeriesRenderStyle chartXYSeriesRenderStyle = null;

    /**
     * Constructor
     *
     * @param name
     * @param xData
     * @param yData
     * @param errorBars
     */
    public SeriesXY(String name, List<?> xData, List<? extends Number> yData, List<? extends Number> errorBars) {

        super(name, xData, yData, errorBars);
    }

    public ChartXYSeriesRenderStyle getChartXYSeriesRenderStyle() {

        return chartXYSeriesRenderStyle;
    }

    public void setChartXYSeriesRenderStyle(ChartXYSeriesRenderStyle chartXYSeriesRenderStyle) {

        this.chartXYSeriesRenderStyle = chartXYSeriesRenderStyle;
    }

    @Override
    public LegendRenderType getLegendRenderType() {

        return chartXYSeriesRenderStyle.getLegendRenderType();
    }

    @Override
    public AxisDataType getAxesType(List<?> data) {

        AxisDataType axisType;

        Iterator<?> itr = data.iterator();
        Object dataPoint = itr.next();
        if (dataPoint instanceof Number) {
            axisType = AxisDataType.Number;
        } else if (dataPoint instanceof Instant) {
            axisType = AxisDataType.Instant;
        } else {
            throw new IllegalArgumentException("series data must be either Number or Instant type");
        }
        return axisType;
    }

    public enum ChartXYSeriesRenderStyle implements RenderableSeries {

        Line(LegendRenderType.Line),

        Area(LegendRenderType.Line),

        Scatter(LegendRenderType.Scatter);

        private final LegendRenderType legendRenderType;

        private ChartXYSeriesRenderStyle(LegendRenderType legendRenderType) {
            this.legendRenderType = legendRenderType;
        }

        @Override
        public LegendRenderType getLegendRenderType() {

            return legendRenderType;
        }
    }
}
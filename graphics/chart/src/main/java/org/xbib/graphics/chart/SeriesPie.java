package org.xbib.graphics.chart;

import org.xbib.graphics.chart.internal.Series;
import org.xbib.graphics.chart.internal.component.RenderableSeries;
import org.xbib.graphics.chart.internal.component.RenderableSeries.LegendRenderType;

/**
 * A Series containing Pie data to be plotted on a Chart
 */
public class SeriesPie extends Series {

    private ChartPieSeriesRenderStyle chartPieSeriesRenderStyle = null;
    private Number value;

    /**
     * Constructor
     *
     * @param name
     * @param value
     */
    public SeriesPie(String name, Number value) {

        super(name);
        this.value = value;
    }

    public ChartPieSeriesRenderStyle getChartPieSeriesRenderStyle() {

        return chartPieSeriesRenderStyle;
    }

    public void setChartPieSeriesRenderStyle(ChartPieSeriesRenderStyle chartPieSeriesRenderStyle) {

        this.chartPieSeriesRenderStyle = chartPieSeriesRenderStyle;
    }

    @Override
    public LegendRenderType getLegendRenderType() {

        return chartPieSeriesRenderStyle.getLegendRenderType();
    }

    public Number getValue() {

        return value;
    }

    public void setValue(Number value) {

        this.value = value;
    }

    public enum ChartPieSeriesRenderStyle implements RenderableSeries {

        Pie(LegendRenderType.Box);

        private final LegendRenderType legendRenderType;

        private ChartPieSeriesRenderStyle(LegendRenderType legendRenderType) {
            this.legendRenderType = legendRenderType;
        }

        @Override
        public LegendRenderType getLegendRenderType() {

            return legendRenderType;
        }
    }

}

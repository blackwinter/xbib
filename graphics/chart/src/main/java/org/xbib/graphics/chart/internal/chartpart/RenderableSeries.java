package org.xbib.graphics.chart.internal.chartpart;

public interface RenderableSeries {

    LegendRenderType getLegendRenderType();

    enum LegendRenderType {

        Line, Scatter, Box
    }

}

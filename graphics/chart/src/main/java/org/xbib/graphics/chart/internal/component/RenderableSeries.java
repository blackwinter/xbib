package org.xbib.graphics.chart.internal.component;

public interface RenderableSeries {

    LegendRenderType getLegendRenderType();

    enum LegendRenderType {

        Line, Scatter, Box
    }

}

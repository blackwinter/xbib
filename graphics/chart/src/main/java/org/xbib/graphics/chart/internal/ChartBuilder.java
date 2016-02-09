package org.xbib.graphics.chart.internal;

import org.xbib.graphics.chart.internal.component.Chart;
import org.xbib.graphics.chart.internal.style.Styler.ChartTheme;

/**
 * A "Builder" to make creating charts easier
 */
public abstract class ChartBuilder<T extends ChartBuilder<?, ?>, C extends Chart> {

    // ChartType chartType = ChartType.XY;
    public int width = 800;
    public int height = 600;
    public String title = "";

    public ChartTheme chartTheme = ChartTheme.XChart;

    /**
     * Constructor
     */
    public ChartBuilder() {
    }

    public T width(int width) {

        this.width = width;
        return (T) this;
    }

    public T height(int height) {

        this.height = height;
        return (T) this;
    }

    public T title(String title) {

        this.title = title;
        return (T) this;
    }

    public T theme(ChartTheme chartTheme) {

        this.chartTheme = chartTheme;
        return (T) this;
    }

    public abstract C build();

}

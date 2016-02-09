package org.xbib.graphics.chart.internal.component;

import org.xbib.graphics.chart.SeriesCategory;
import org.xbib.graphics.chart.SeriesCategory.ChartCategorySeriesRenderStyle;
import org.xbib.graphics.chart.StylerCategory;
import org.xbib.graphics.chart.internal.Series;
import org.xbib.graphics.chart.internal.style.StylerAxesChart;

import java.awt.*;

public class PlotCategory<ST extends StylerAxesChart, S extends Series> extends PlotAxesChart {

    StylerCategory stylerCategory;

    /**
     * Constructor
     *
     * @param chart
     */
    public PlotCategory(Chart<StylerCategory, SeriesCategory> chart) {

        super(chart);
        stylerCategory = chart.getStyler();
    }

    @Override
    public void paint(Graphics2D g) {

        if (ChartCategorySeriesRenderStyle.Bar.equals(stylerCategory.getDefaultSeriesRenderStyle()) || ChartCategorySeriesRenderStyle.Stick.equals(stylerCategory.getDefaultSeriesRenderStyle())) {

            this.plotContent = new PlotContentCategoryBar<StylerCategory, SeriesCategory>(chart);
        } else {
            this.plotContent = new PlotContentCategoryLineAreaScatter<StylerCategory, SeriesCategory>(chart);
        }

        super.paint(g);
    }

}

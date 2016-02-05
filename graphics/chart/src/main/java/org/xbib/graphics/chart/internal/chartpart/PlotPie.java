package org.xbib.graphics.chart.internal.chartpart;

import org.xbib.graphics.chart.SeriesPie;
import org.xbib.graphics.chart.StylerPie;
import org.xbib.graphics.chart.internal.Series;
import org.xbib.graphics.chart.internal.style.Styler;
import org.xbib.graphics.chart.internal.style.Styler.LegendPosition;

import java.awt.*;
import java.awt.geom.Rectangle2D;

public class PlotPie<ST extends Styler, S extends Series> extends Plot {

    /**
     * Constructor
     *
     * @param chart
     */
    public PlotPie(Chart<StylerPie, SeriesPie> chart) {

        super(chart);
        this.plotContent = new PlotContentPie<StylerPie, SeriesPie>(chart);
        this.plotSurface = new PlotSurfacePie<StylerPie, SeriesPie>(chart);
    }

    @Override
    public void paint(Graphics2D g) {

        // calculate bounds
        double xOffset = chart.getStyler().getChartPadding();

        // double yOffset = chart.getChartTitle().getBounds().getHeight() + 2 * chart.getStyler().getChartPadding();
        double yOffset = chart.getChartTitle().getBounds().getHeight() + chart.getStyler().getChartPadding();

        double width =
                chart.getWidth()
                        - (chart.getStyler().getLegendPosition() == LegendPosition.OutsideE ? chart.getLegend().getBounds().getWidth() : 0)
                        - 2 * chart.getStyler().getChartPadding()
                        - (chart.getStyler().getLegendPosition() == LegendPosition.OutsideE && chart.getStyler().isLegendVisible() ? chart.getStyler().getChartPadding() : 0);

        double height = chart.getHeight() - chart.getChartTitle().getBounds().getHeight() - 2 * chart.getStyler().getChartPadding();

        this.bounds = new Rectangle2D.Double(xOffset, yOffset, width, height);

        super.paint(g);
    }
}

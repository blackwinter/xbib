package org.xbib.graphics.chart.internal.component;

import org.xbib.graphics.chart.SeriesPie;
import org.xbib.graphics.chart.StylerPie;
import org.xbib.graphics.chart.internal.Series;
import org.xbib.graphics.chart.internal.style.StylerAxesChart;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.Map;

public class LegendPie<ST extends StylerAxesChart, S extends Series> extends Legend {

    /**
     * Constructor
     *
     * @param chart
     */
    public LegendPie(Chart<StylerPie, SeriesPie> chart) {
        super(chart);
    }

    @Override
    public void paint(Graphics2D g) {

        if (!chart.getStyler().isLegendVisible()) {
            return;
        }

        super.paint(g);

        g.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 10.0f, new float[]{3.0f, 0.0f}, 0.0f));

        // Draw legend content inside legend box
        double startx = xOffset + chart.getStyler().getLegendPadding();
        double starty = yOffset + chart.getStyler().getLegendPadding();

        Map<String, Series> map = chart.getSeriesMap();
        for (Series series : map.values()) {

            if (!series.isShowInLegend()) {
                continue;
            }

            Map<String, Rectangle2D> seriesTextBounds = getSeriesTextBounds(series);

            float legendEntryHeight = 0;
            double legendTextContentMaxWidth = 0; // TODO 3.0.0 don't need this
            for (Map.Entry<String, Rectangle2D> entry : seriesTextBounds.entrySet()) {
                legendEntryHeight += entry.getValue().getHeight() + MULTI_LINE_SPACE;
                legendTextContentMaxWidth = Math.max(legendTextContentMaxWidth, entry.getValue().getWidth());
            }
            legendEntryHeight -= MULTI_LINE_SPACE;

            legendEntryHeight = Math.max(legendEntryHeight, BOX_SIZE);

            // bar/pie type series

            // paint little box
            if (series.getFillColor() != null) {
                g.setColor(series.getFillColor());
                Shape rectSmall = new Rectangle2D.Double(startx, starty, BOX_SIZE, BOX_SIZE);
                g.fill(rectSmall);
            }

            g.setColor(chart.getStyler().getChartFontColor());

            double multiLineOffset = 0.0;

            // bar/pie type series

            final double x = startx + BOX_SIZE + chart.getStyler().getLegendPadding();
            for (Map.Entry<String, Rectangle2D> entry : seriesTextBounds.entrySet()) {

                double height = entry.getValue().getHeight();
                double centerOffsetY = (Math.max(BOX_SIZE, height) - height) / 2.0;

                FontRenderContext frc = g.getFontRenderContext();
                TextLayout tl = new TextLayout(entry.getKey(), chart.getStyler().getLegendFont(), frc);
                Shape shape = tl.getOutline(null);
                AffineTransform orig = g.getTransform();
                AffineTransform at = new AffineTransform();
                at.translate(x, starty + height + centerOffsetY + multiLineOffset);
                g.transform(at);
                g.fill(shape);
                g.setTransform(orig);

                multiLineOffset += height + MULTI_LINE_SPACE;

            }
            starty += legendEntryHeight + chart.getStyler().getLegendPadding();

        }

        // bounds
        bounds = new Rectangle2D.Double(xOffset, yOffset, bounds.getWidth(), bounds.getHeight());


    }

    @Override
    public Rectangle2D getBounds() {

        if (bounds == null) { // was not drawn fully yet, just need the height hint. The Axis object may be asking for it.
            bounds = getBoundsHint();
        }
        return bounds;
    }

    @Override
    public double getSeriesLegendRenderGraphicHeight(Series series) {

        return BOX_SIZE;
    }
}

package org.xbib.graphics.chart.internal.component;

import org.xbib.graphics.chart.SeriesXY;
import org.xbib.graphics.chart.internal.Series;
import org.xbib.graphics.chart.internal.SeriesAxesChart;
import org.xbib.graphics.chart.internal.component.RenderableSeries.LegendRenderType;
import org.xbib.graphics.chart.internal.style.StylerAxesChart;
import org.xbib.graphics.chart.internal.style.lines.SeriesLines;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.Map;

public class LegendAxesChart<ST extends StylerAxesChart, S extends Series> extends Legend {

    StylerAxesChart stylerAxesChart;

    /**
     * Constructor
     *
     * @param chart
     */
    public LegendAxesChart(Chart<StylerAxesChart, SeriesXY> chart) {

        super(chart);
        stylerAxesChart = chart.getStyler();
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

        Map<String, SeriesAxesChart> map = chart.getSeriesMap();
        for (SeriesAxesChart series : map.values()) {

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

            legendEntryHeight = Math.max(legendEntryHeight, (series.getLegendRenderType() == LegendRenderType.Box ? BOX_SIZE : stylerAxesChart.getMarkerSize()));

            // paint line and marker
            if (series.getLegendRenderType() != LegendRenderType.Box) {

                // paint line
                if (series.getLegendRenderType() == LegendRenderType.Line && series.getLineStyle() != SeriesLines.NONE) {
                    g.setColor(series.getLineColor());
                    g.setStroke(series.getLineStyle());
                    Shape line = new Line2D.Double(startx, starty + legendEntryHeight / 2.0, startx + chart.getStyler().getLegendSeriesLineLength(), starty + legendEntryHeight / 2.0);
                    g.draw(line);
                }
                // paint marker
                if (series.getMarker() != null) {
                    g.setColor(series.getMarkerColor());
                    series.getMarker().paint(g, startx + chart.getStyler().getLegendSeriesLineLength() / 2.0, starty + legendEntryHeight / 2.0, stylerAxesChart.getMarkerSize());

                }
            } else { // bar/pie type series

                // paint little box
                Shape rectSmall = new Rectangle2D.Double(startx, starty, BOX_SIZE, BOX_SIZE);
                g.setColor(series.getFillColor());
                g.fill(rectSmall);
            }

            g.setColor(chart.getStyler().getChartFontColor());

            double multiLineOffset = 0.0;

            if (series.getLegendRenderType() != LegendRenderType.Box) {

                double x = startx + chart.getStyler().getLegendSeriesLineLength() + chart.getStyler().getLegendPadding();
                for (Map.Entry<String, Rectangle2D> entry : seriesTextBounds.entrySet()) {

                    double height = entry.getValue().getHeight();
                    double centerOffsetY = (Math.max(stylerAxesChart.getMarkerSize(), height) - height) / 2.0;

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

            } else { // bar/pie type series

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

        return series.getLegendRenderType() == LegendRenderType.Box ? BOX_SIZE : stylerAxesChart.getMarkerSize();
    }
}

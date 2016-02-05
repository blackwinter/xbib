package org.xbib.graphics.chart.internal.chartpart;

import org.xbib.graphics.chart.SeriesPie;
import org.xbib.graphics.chart.StylerPie;
import org.xbib.graphics.chart.StylerPie.AnnotationType;
import org.xbib.graphics.chart.internal.Series;
import org.xbib.graphics.chart.internal.style.Styler;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.util.Map;

public class PlotContentPie<ST extends Styler, S extends Series> extends PlotContent {

    StylerPie stylerPie;
    DecimalFormat df = new DecimalFormat("#.0");

    /**
     * Constructor
     *
     * @param chart
     */
    protected PlotContentPie(Chart<StylerPie, SeriesPie> chart) {

        super(chart);
        stylerPie = chart.getStyler();
    }

    @Override
    public void paint(Graphics2D g) {

        // plot area bounds
        Rectangle2D bounds = getBounds();
        // g.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
        // g.setColor(Color.red);
        // g.draw(bounds);

        // if the area to draw a chart on is so small, don't even bother
        if (bounds.getWidth() < 30) {
            return;
        }

        Rectangle2D rectangle = new Rectangle2D.Double(0, 0, chart.getWidth(), chart.getHeight());
        g.setClip(bounds.createIntersection(rectangle));

        // pie bounds
        double pieFillPercentage = stylerPie.getPlotContentSize();


        double halfBorderPercentage = (1 - pieFillPercentage) / 2.0;
        double width = stylerPie.isCircular() ? Math.min(bounds.getWidth(), bounds.getHeight()) : bounds.getWidth();
        double height = stylerPie.isCircular() ? Math.min(bounds.getWidth(), bounds.getHeight()) : bounds.getHeight();

        Rectangle2D pieBounds = new Rectangle2D.Double(
                bounds.getX() + bounds.getWidth() / 2 - width / 2 + halfBorderPercentage * width,
                bounds.getY() + bounds.getHeight() / 2 - height / 2 + halfBorderPercentage * height,
                width * pieFillPercentage,
                height * pieFillPercentage);


        // get total
        double total = 0.0;

        Map<String, SeriesPie> map = chart.getSeriesMap();
        for (SeriesPie series : map.values()) {

            total += series.getValue().doubleValue();
        }

        // draw pie slices
        // double curValue = 0.0;
        // double curValue = 0.0;
        double startAngle = stylerPie.getStartAngleInDegrees() + 90;

        map = chart.getSeriesMap();
        for (SeriesPie series : map.values()) {

            Number y = series.getValue();

            // draw slice
            double arcAngle = (y.doubleValue() * 360 / total);
            g.setColor(series.getFillColor());
            g.fill(new Arc2D.Double(pieBounds.getX(), pieBounds.getY(), pieBounds.getWidth(), pieBounds.getHeight(), startAngle, arcAngle, Arc2D.PIE));
            g.setColor(stylerPie.getPlotBackgroundColor());
            g.draw(new Arc2D.Double(pieBounds.getX(), pieBounds.getY(), pieBounds.getWidth(), pieBounds.getHeight(), startAngle, arcAngle, Arc2D.PIE));
            // curValue += y.doubleValue();

            // draw annotation
            String annotation = "";
            if (stylerPie.getAnnotationType() == AnnotationType.Label) {
                annotation = series.getName();
            } else if (stylerPie.getAnnotationType() == AnnotationType.LabelAndPercentage) {
                double percentage = y.doubleValue() / total * 100;
                annotation = series.getName() + " (" + df.format(percentage) + "%)";
            } else if (stylerPie.getAnnotationType() == AnnotationType.Percentage) {
                double percentage = y.doubleValue() / total * 100;
                annotation = df.format(percentage) + "%";
            }

            TextLayout textLayout = new TextLayout(annotation, stylerPie.getAnnotationFont(), new FontRenderContext(null, true, false));
            Rectangle2D percentageRectangle = textLayout.getBounds();

            double xCenter = pieBounds.getX() + pieBounds.getWidth() / 2 - percentageRectangle.getWidth() / 2;
            double yCenter = pieBounds.getY() + pieBounds.getHeight() / 2 + percentageRectangle.getHeight() / 2;
            double angle = (arcAngle + startAngle) - arcAngle / 2;
            double xOffset = xCenter + Math.cos(Math.toRadians(angle)) * (pieBounds.getWidth() / 2 * stylerPie.getAnnotationDistance());
            double yOffset = yCenter - Math.sin(Math.toRadians(angle)) * (pieBounds.getHeight() / 2 * stylerPie.getAnnotationDistance());

            // get annotation width
            Shape shape = textLayout.getOutline(null);
            Rectangle2D annotationBounds = shape.getBounds2D();
            double annotationWidth = annotationBounds.getWidth();
            double annotationHeight = annotationBounds.getHeight();

            // get slice area
            double xOffset1 = xCenter + Math.cos(Math.toRadians(startAngle)) * (pieBounds.getWidth() / 2 * stylerPie.getAnnotationDistance());
            double yOffset1 = yCenter - Math.sin(Math.toRadians(startAngle)) * (pieBounds.getHeight() / 2 * stylerPie.getAnnotationDistance());
            double xOffset2 = xCenter + Math.cos(Math.toRadians((arcAngle + startAngle))) * (pieBounds.getWidth() / 2 * stylerPie.getAnnotationDistance());
            double yOffset2 = yCenter - Math.sin(Math.toRadians((arcAngle + startAngle))) * (pieBounds.getHeight() / 2 * stylerPie.getAnnotationDistance());
            double xDiff = Math.abs(xOffset1 - xOffset2);
            double yDiff = Math.abs(yOffset1 - yOffset2);
            boolean annotationWillFit = false;
            if (xDiff >= yDiff) { // assume more vertically orientated slice
                if (annotationWidth < xDiff) {
                    annotationWillFit = true;
                }
            } else if (xDiff <= yDiff) { // assume more horizontally orientated slice
                if (annotationHeight < yDiff) {
                    annotationWillFit = true;
                }
            }

            // draw annotation
            if (annotationWillFit) {

                g.setColor(stylerPie.getChartFontColor());
                g.setFont(stylerPie.getChartTitleFont());
                AffineTransform orig = g.getTransform();
                AffineTransform at = new AffineTransform();

                // inside
                if (stylerPie.getAnnotationDistance() <= 1.0) {
                    at.translate(xOffset, yOffset);
                }

                // outside
                else {

                    // Tick Mark
                    xCenter = pieBounds.getX() + pieBounds.getWidth() / 2;
                    yCenter = pieBounds.getY() + pieBounds.getHeight() / 2;
                    // double endPoint = Math.min((2.0 - (stylerPie.getAnnotationDistance() - 1)), 1.95);
                    double endPoint = (3.0 - stylerPie.getAnnotationDistance());
                    double xOffsetStart = xCenter + Math.cos(Math.toRadians(angle)) * (pieBounds.getWidth() / 2.01);
                    double xOffsetEnd = xCenter + Math.cos(Math.toRadians(angle)) * (pieBounds.getWidth() / endPoint);
                    double yOffsetStart = yCenter - Math.sin(Math.toRadians(angle)) * (pieBounds.getHeight() / 2.01);
                    double yOffsetEnd = yCenter - Math.sin(Math.toRadians(angle)) * (pieBounds.getHeight() / endPoint);

                    g.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
                    Shape line = new Line2D.Double(xOffsetStart, yOffsetStart, xOffsetEnd, yOffsetEnd);
                    g.draw(line);

                    // annotation
                    at.translate(xOffset - Math.sin(Math.toRadians(angle - 90)) * annotationWidth / 2 + 3, yOffset);

                }

                g.transform(at);
                g.fill(shape);
                g.setTransform(orig);

            }

            startAngle += arcAngle;
        }

        g.setClip(null);

    }

}

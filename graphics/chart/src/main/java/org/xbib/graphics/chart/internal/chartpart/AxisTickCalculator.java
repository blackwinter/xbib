package org.xbib.graphics.chart.internal.chartpart;

import org.xbib.graphics.chart.internal.chartpart.Axis.Direction;
import org.xbib.graphics.chart.internal.style.StylerAxesChart;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;
import java.util.List;

public abstract class AxisTickCalculator {

    protected final Direction axisDirection;
    ;
    protected final double workingSpace;
    protected final double minValue;
    protected final double maxValue;
    protected final StylerAxesChart styler;
    /**
     * the List of tick label position in pixels
     */
    protected List<Double> tickLocations = new LinkedList<Double>();
    /**
     * the List of tick label values
     */
    protected List<String> tickLabels = new LinkedList<String>();

    /**
     * Constructor
     *
     * @param axisDirection
     * @param workingSpace
     * @param minValue
     * @param maxValue
     * @param styler
     */
    public AxisTickCalculator(Direction axisDirection, double workingSpace, double minValue, double maxValue, StylerAxesChart styler) {

        this.axisDirection = axisDirection;
        this.workingSpace = workingSpace;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.styler = styler;
    }

    /**
     * Gets the first position
     *
     * @param gridStep
     * @return
     */
    double getFirstPosition(double gridStep) {

        // System.out.println("******");

        double firstPosition = minValue - (minValue % gridStep) - gridStep;
        return firstPosition;
    }

    public List<Double> getTickLocations() {

        return tickLocations;
    }

    public List<String> getTickLabels() {

        return tickLabels;
    }

    /**
     * Given the generated tickLabels, will they fit side-by-side without overlapping each other and looking bad?
     * Sometimes the given tickSpacingHint is simply too small.
     *
     * @param tickLabels
     * @param tickSpacingHint
     * @return
     */
    boolean willLabelsFitInTickSpaceHint(List<String> tickLabels, int tickSpacingHint) {

        // Assume that for Y-Axis the ticks will all fit based on their tickSpace hint because the text is usually horizontal and "short". This more applies to the X-Axis.
        if (this.axisDirection == Direction.Y) {
            return true;
        }

        String sampleLabel = " ";
        for (String tickLabel : tickLabels) {
            if (tickLabel != null && tickLabel.length() > sampleLabel.length()) {
                sampleLabel = tickLabel;
            }
        }

        TextLayout textLayout = new TextLayout(sampleLabel, styler.getAxisTickLabelsFont(), new FontRenderContext(null, true, false));
        AffineTransform rot = styler.getXAxisLabelRotation() == 0 ? null : AffineTransform.getRotateInstance(-1 * Math.toRadians(styler.getXAxisLabelRotation()));
        Shape shape = textLayout.getOutline(rot);
        Rectangle2D rectangle = shape.getBounds();
        double largestLabelWidth = rectangle.getWidth();
        return (largestLabelWidth * 1.1 < tickSpacingHint);

    }
}

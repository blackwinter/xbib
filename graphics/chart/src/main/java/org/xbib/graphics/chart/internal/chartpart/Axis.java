package org.xbib.graphics.chart.internal.chartpart;

import org.xbib.graphics.chart.StylerCategory;
import org.xbib.graphics.chart.internal.Series;
import org.xbib.graphics.chart.internal.SeriesAxesChart;
import org.xbib.graphics.chart.internal.style.Styler.LegendPosition;
import org.xbib.graphics.chart.internal.style.StylerAxesChart;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.List;

/**
 * Axis
 */
public class Axis<ST extends StylerAxesChart, S extends Series> implements ChartPart {

    private final Chart<StylerAxesChart, SeriesAxesChart> chart;
    private Rectangle2D bounds;
    /**
     * the paint zone
     */
    private Rectangle2D paintZone;
    /**
     * the axisDataType
     */
    private AxisDataType axisDataType;
    /**
     * the axis title
     */
    private AxisTitle<StylerAxesChart, SeriesAxesChart> axisTitle;
    /**
     * the axis tick
     */
    private AxisTick<StylerAxesChart, SeriesAxesChart> axisTick;
    /**
     * the axis tick calculator
     */
    private AxisTickCalculator axisTickCalculator;
    /**
     * the axis direction
     */
    private Direction direction;
    private double min;
    private double max;

    /**
     * Constructor
     *
     * @param chart     the Chart
     * @param direction the axis direction (X or Y)
     */
    public Axis(Chart<StylerAxesChart, SeriesAxesChart> chart, Direction direction) {

        this.chart = chart;
        this.direction = direction;
        axisTitle = new AxisTitle<StylerAxesChart, SeriesAxesChart>(chart, direction);
        axisTick = new AxisTick<StylerAxesChart, SeriesAxesChart>(chart, direction);
        // resetMinMax();
    }

    protected Rectangle2D getPaintZone() {

        return paintZone;
    }

    /**
     * Reset the default min and max values in preparation for calculating the actual min and max
     */
    protected void resetMinMax() {

        min = Double.MAX_VALUE;
        max = -Double.MAX_VALUE;
    }

    /**
     * @param min
     * @param max
     */
    protected void addMinMax(double min, double max) {

        // System.out.println(min);
        // System.out.println(max);
        // NaN indicates String axis data, so min and max play no role
        if (this.min == Double.NaN || min < this.min) {
            this.min = min;
        }
        if (this.max == Double.NaN || max > this.max) {
            this.max = max;
        }

        // System.out.println(this.min);
        // System.out.println(this.max);
    }

    @Override
    public void paint(Graphics2D g) {

        paintZone = new Rectangle2D.Double();
        bounds = new Rectangle2D.Double();

        // determine Axis bounds
        if (direction == Direction.Y) { // Y-Axis - gets called first

            // first determine the height of

            // calculate paint zone
            // ----
            // |
            // |
            // |
            // |
            // ----
            double xOffset = chart.getStyler().getChartPadding();
            // double yOffset = chart.getChartTitle().getBounds().getHeight() < .1 ? chart.getStyler().getChartPadding() : chart.getChartTitle().getBounds().getHeight()
            // + chart.getStyler().getChartPadding();
            double yOffset = chart.getChartTitle().getBounds().getHeight() + chart.getStyler().getChartPadding();

            /////////////////////////
            int i = 1; // just twice through is all it takes
            double width = 60; // arbitrary, final width depends on Axis tick labels
            double height = 0;
            do {
                // System.out.println("width before: " + width);

                double approximateXAxisWidth =

                        chart.getWidth()

                                - width // y-axis approx. width

                                - (chart.getStyler().getLegendPosition() == LegendPosition.OutsideE ? chart.getLegend().getBounds().getWidth() : 0)

                                - 2 * chart.getStyler().getChartPadding()

                                - (chart.getStyler().isYAxisTicksVisible() ? (chart.getStyler().getPlotMargin()) : 0)

                                - (chart.getStyler().getLegendPosition() == LegendPosition.OutsideE && chart.getStyler().isLegendVisible() ? chart.getStyler().getChartPadding() : 0);

                height = chart.getHeight() - yOffset - chart.getXAxis().getXAxisHeightHint(approximateXAxisWidth) - chart.getStyler().getPlotMargin() - chart.getStyler().getChartPadding();

                width = getYAxisWidthHint(height);
                // System.out.println("width after: " + width);

                // System.out.println("height: " + height);

            } while (i-- > 0);

            /////////////////////////

            Rectangle2D yAxisRectangle = new Rectangle2D.Double(xOffset, yOffset, width, height);
            this.paintZone = yAxisRectangle;
            // g.setColor(Color.green);
            // g.draw(yAxisRectangle);

            // fill in Axis with sub-components
            axisTitle.paint(g);
            axisTick.paint(g);

            xOffset = paintZone.getX();
            yOffset = paintZone.getY();
            width = (chart.getStyler().isYAxisTitleVisible() ? axisTitle.getBounds().getWidth() : 0) + axisTick.getBounds().getWidth();
            height = paintZone.getHeight();
            bounds = new Rectangle2D.Double(xOffset, yOffset, width, height);

            // g.setColor(Color.yellow);
            // g.draw(bounds);

        } else { // X-Axis

            // calculate paint zone
            // |____________________|

            double xOffset = chart.getYAxis().getBounds().getWidth() + (chart.getStyler().isYAxisTicksVisible() ? chart.getStyler().getPlotMargin() : 0) + chart.getStyler().getChartPadding();
            double yOffset = chart.getYAxis().getBounds().getY() + chart.getYAxis().getBounds().getHeight() + chart.getStyler().getPlotMargin();

            double width =

                    chart.getWidth()

                            - chart.getYAxis().getBounds().getWidth() // y-axis was already painted

                            - (chart.getStyler().getLegendPosition() == LegendPosition.OutsideE ? chart.getLegend().getBounds().getWidth() : 0)

                            - 2 * chart.getStyler().getChartPadding()

                            - (chart.getStyler().isYAxisTicksVisible() ? (chart.getStyler().getPlotMargin()) : 0)

                            - (chart.getStyler().getLegendPosition() == LegendPosition.OutsideE && chart.getStyler().isLegendVisible() ? chart.getStyler().getChartPadding() : 0);

            // double height = this.getXAxisHeightHint(width);
            // System.out.println("height: " + height);
            // the Y-Axis was already draw at this point so we know how much vertical room is left for the X-Axis
            double height = chart.getHeight() - chart.getYAxis().getBounds().getY() - chart.getYAxis().getBounds().getHeight() - chart.getStyler().getChartPadding() - chart.getStyler().getPlotMargin();
            // System.out.println("height2: " + height2);

            Rectangle2D xAxisRectangle = new Rectangle2D.Double(xOffset, yOffset, width, height);

            // the paint zone
            this.paintZone = xAxisRectangle;
            // g.setColor(Color.green);
            // g.draw(xAxisRectangle);

            // now paint the X-Axis given the above paint zone
            axisTitle.paint(g);
            axisTick.paint(g);

            bounds = paintZone;

            // g.setColor(Color.yellow);
            // g.draw(bounds);
        }

    }

    /**
     * The vertical Y-Axis is drawn first, but to know the lower bounds of it, we need to know how high the X-Axis paint
     * zone is going to be. Since the tick labels could be rotated, we need to actually
     * determine the tick labels first to get an idea of how tall the X-Axis tick labels will be.
     *
     * @return
     */
    private double getXAxisHeightHint(double workingSpace) {

        // Axis title
        double titleHeight = 0.0;
        if (chart.getXAxisTitle() != null && !chart.getXAxisTitle().trim().equalsIgnoreCase("") && chart.getStyler().isXAxisTitleVisible()) {
            TextLayout textLayout = new TextLayout(chart.getXAxisTitle(), chart.getStyler().getAxisTitleFont(), new FontRenderContext(null, true, false));
            Rectangle2D rectangle = textLayout.getBounds();
            titleHeight = rectangle.getHeight() + chart.getStyler().getAxisTitlePadding();
        }

        // Axis tick labels
        double axisTickLabelsHeight = 0.0;
        if (chart.getStyler().isXAxisTicksVisible()) {

            // get some real tick labels
            // System.out.println("XAxisHeightHint");
            // System.out.println("workingSpace: " + workingSpace);
            this.axisTickCalculator = getAxisTickCalculator(workingSpace);

            String sampleLabel = "";
            // find the longest String in all the labels
            for (int i = 0; i < axisTickCalculator.getTickLabels().size(); i++) {
                // System.out.println("label: " + axisTickCalculator.getTickLabels().get(i));
                if (axisTickCalculator.getTickLabels().get(i) != null && axisTickCalculator.getTickLabels().get(i).length() > sampleLabel.length()) {
                    sampleLabel = axisTickCalculator.getTickLabels().get(i);
                }
            }
            // System.out.println("sampleLabel: " + sampleLabel);

            // get the height of the label including rotation
            TextLayout textLayout = new TextLayout(sampleLabel.length() == 0 ? " " : sampleLabel, chart.getStyler().getAxisTickLabelsFont(), new FontRenderContext(null, true, false));
            AffineTransform rot = chart.getStyler().getXAxisLabelRotation() == 0 ? null : AffineTransform.getRotateInstance(-1 * Math.toRadians(chart.getStyler().getXAxisLabelRotation()));
            Shape shape = textLayout.getOutline(rot);
            Rectangle2D rectangle = shape.getBounds();

            axisTickLabelsHeight = rectangle.getHeight() + chart.getStyler().getAxisTickPadding() + chart.getStyler().getAxisTickMarkLength();
        }
        return titleHeight + axisTickLabelsHeight;
    }

    private double getYAxisWidthHint(double workingSpace) {

        // Axis title
        double titleHeight = 0.0;
        if (chart.getyYAxisTitle() != null && !chart.getyYAxisTitle().trim().equalsIgnoreCase("") && chart.getStyler().isYAxisTitleVisible()) {
            TextLayout textLayout = new TextLayout(chart.getyYAxisTitle(), chart.getStyler().getAxisTitleFont(), new FontRenderContext(null, true, false));
            Rectangle2D rectangle = textLayout.getBounds();
            titleHeight = rectangle.getHeight() + chart.getStyler().getAxisTitlePadding();
        }

        // Axis tick labels
        double axisTickLabelsHeight = 0.0;
        if (chart.getStyler().isYAxisTicksVisible()) {

            // get some real tick labels
            // System.out.println("XAxisHeightHint");
            // System.out.println("workingSpace: " + workingSpace);
            this.axisTickCalculator = getAxisTickCalculator(workingSpace);

            String sampleLabel = "";
            // find the longest String in all the labels
            for (int i = 0; i < axisTickCalculator.getTickLabels().size(); i++) {
                if (axisTickCalculator.getTickLabels().get(i) != null && axisTickCalculator.getTickLabels().get(i).length() > sampleLabel.length()) {
                    sampleLabel = axisTickCalculator.getTickLabels().get(i);
                }
            }

            // get the height of the label including rotation
            TextLayout textLayout = new TextLayout(sampleLabel.length() == 0 ? " " : sampleLabel, chart.getStyler().getAxisTickLabelsFont(), new FontRenderContext(null, true, false));
            Rectangle2D rectangle = textLayout.getBounds();

            axisTickLabelsHeight = rectangle.getWidth() + chart.getStyler().getAxisTickPadding() + chart.getStyler().getAxisTickMarkLength();
        }
        return titleHeight + axisTickLabelsHeight;
    }

    private AxisTickCalculator getAxisTickCalculator(double workingSpace) {

        // X-Axis
        if (getDirection() == Direction.X) {

            if (chart.getStyler() instanceof StylerCategory) {

                List<?> categories = (List<?>) chart.getSeriesMap().values().iterator().next().getXData();
                AxisDataType axisType = chart.getAxisPair().getXAxis().getAxisDataType();

                return new AxisTickCalculatorCategory(getDirection(), workingSpace, categories, axisType, chart.getStyler());

            } else if (getAxisDataType() == AxisDataType.Date) {

                return new AxisTickCalculatorDate(getDirection(), workingSpace, min, max, chart.getStyler());
            } else if (chart.getStyler().isXAxisLogarithmic()) {

                return new AxisTickCalculatorLogarithmic(getDirection(), workingSpace, min, max, chart.getStyler());
            } else {
                return new AxisTickCalculatorNumber(getDirection(), workingSpace, min, max, chart.getStyler());

            }
        }

        // Y-Axis
        else {

            if (chart.getStyler().isYAxisLogarithmic() && getAxisDataType() != AxisDataType.Date) {

                return new AxisTickCalculatorLogarithmic(getDirection(), workingSpace, min, max, chart.getStyler());
            } else {
                return new AxisTickCalculatorNumber(getDirection(), workingSpace, min, max, chart.getStyler());

            }
        }

    }

    protected AxisDataType getAxisDataType() {

        return axisDataType;
    }

    public void setAxisDataType(AxisDataType axisDataType) {

        if (axisDataType != null && this.axisDataType != null && this.axisDataType != axisDataType) {
            throw new IllegalArgumentException("Different Axes (e.g. Date, Number, String) cannot be mixed on the same chart!!");
        }
        this.axisDataType = axisDataType;
    }

    // Getters /////////////////////////////////////////////////

    protected double getMin() {

        return min;
    }

    protected void setMin(double min) {

        this.min = min;
    }

    protected double getMax() {

        return max;
    }

    protected void setMax(double max) {

        this.max = max;
    }

    protected AxisTick<StylerAxesChart, SeriesAxesChart> getAxisTick() {

        return axisTick;
    }

    protected Direction getDirection() {

        return direction;
    }

    protected AxisTitle<StylerAxesChart, SeriesAxesChart> getAxisTitle() {

        return axisTitle;
    }

    public AxisTickCalculator getAxisTickCalculator() {

        return this.axisTickCalculator;
    }

    @Override
    public Rectangle2D getBounds() {

        return bounds;
    }

    public enum AxisDataType {

        Number, Date, String;
    }

    /**
     * An axis direction
     */
    public enum Direction {

        /**
         * the constant to represent X axis
         */
        X,

        /**
         * the constant to represent Y axis
         */
        Y
    }
}

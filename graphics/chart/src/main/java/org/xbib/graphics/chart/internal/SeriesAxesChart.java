package org.xbib.graphics.chart.internal;

import org.xbib.graphics.chart.internal.chartpart.Axis.AxisDataType;
import org.xbib.graphics.chart.internal.style.markers.Marker;

import java.awt.*;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * A Series containing X and Y data to be plotted on a Chart with X and Y Axes
 */
public abstract class SeriesAxesChart extends Series {

    private List<?> xData; // can be Number or Date or String
    private AxisDataType xAxisType;
    private List<? extends Number> yData;
    private AxisDataType yAxisType;
    private List<? extends Number> errorBars;
    /**
     * the minimum value of axis range
     */
    private double xMin;
    /**
     * the maximum value of axis range
     */
    private double xMax;
    /**
     * the minimum value of axis range
     */
    private double yMin;
    /**
     * the maximum value of axis range
     */
    private double yMax;
    /**
     * Line Style
     */
    private BasicStroke stroke;
    /**
     * Line Color
     */
    private Color lineColor;
    /**
     * Marker Style
     */
    private Marker marker;
    /**
     * Marker Color
     */
    private Color markerColor;

    /**
     * Constructor
     *
     * @param name
     * @param xData
     * @param yData
     * @param errorBars
     */
    public SeriesAxesChart(String name, List<?> xData, List<? extends Number> yData, List<? extends Number> errorBars) {

        super(name);

        this.xData = xData;
        this.xAxisType = getAxesType(xData);
        this.yData = yData;
        this.yAxisType = AxisDataType.Number;
        this.errorBars = errorBars;

        calculateMinMax();
    }

    public abstract AxisDataType getAxesType(List<?> data);

    public void replaceData(List<?> newXData, List<? extends Number> newYData, List<? extends Number> newErrorBars) {

        // Sanity check
        if (newErrorBars != null && newErrorBars.size() != newYData.size()) {
            throw new IllegalArgumentException("error bars and Y-Axis sizes are not the same!!!");
        }
        if (newXData.size() != newYData.size()) {
            throw new IllegalArgumentException("X and Y-Axis sizes are not the same!!!");
        }

        xData = newXData;
        yData = newYData;
        errorBars = newErrorBars;
        calculateMinMax();
    }

    private void calculateMinMax() {

        // xData
        double[] xMinMax = findMinMax(xData, xAxisType);
        xMin = xMinMax[0];
        xMax = xMinMax[1];
        // System.out.println(xMin);
        // System.out.println(xMax);

        // yData
        double[] yMinMax = null;
        if (errorBars == null) {
            yMinMax = findMinMax(yData, yAxisType);
        } else {
            yMinMax = findMinMaxWithErrorBars(yData, errorBars);
        }
        yMin = yMinMax[0];
        yMax = yMinMax[1];
    }

    /**
     * Finds the min and max of a dataset
     *
     * @param data
     * @return
     */
    private double[] findMinMax(Collection<?> data, AxisDataType axisType) {

        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;

        for (Object dataPoint : data) {

            if (dataPoint == null) {
                continue;
            }

            double value = 0.0;

            if (axisType == AxisDataType.Number) {
                value = ((Number) dataPoint).doubleValue();
            } else if (axisType == AxisDataType.Date) {
                Date date = (Date) dataPoint;
                value = date.getTime();
            } else if (axisType == AxisDataType.String) {
                return new double[]{Double.NaN, Double.NaN};
            }
            if (value < min) {
                min = value;
            }
            if (value > max) {
                max = value;
            }
        }

        return new double[]{min, max};
    }

    /**
     * Finds the min and max of a dataset accounting for error bars
     *
     * @param data
     * @return
     */
    private double[] findMinMaxWithErrorBars(Collection<? extends Number> data, Collection<? extends Number> errorBars) {

        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;

        Iterator<? extends Number> itr = data.iterator();
        Iterator<? extends Number> ebItr = errorBars.iterator();
        while (itr.hasNext()) {
            double bigDecimal = itr.next().doubleValue();
            double eb = ebItr.next().doubleValue();
            if (bigDecimal - eb < min) {
                min = bigDecimal - eb;
            }
            if (bigDecimal + eb > max) {
                max = bigDecimal + eb;
            }
        }
        return new double[]{min, max};
    }

    /**
     * Set the line style of the series
     *
     * @param basicStroke
     */
    public Series setLineStyle(BasicStroke basicStroke) {

        stroke = basicStroke;
        return this;
    }

    /**
     * Set the line color of the series
     *
     * @param color
     */
    public Series setLineColor(java.awt.Color color) {

        this.lineColor = color;
        return this;
    }

    /**
     * Sets the marker for the series
     *
     * @param marker
     */
    public Series setMarker(Marker marker) {

        this.marker = marker;
        return this;
    }

    /**
     * Sets the marker color for the series
     *
     * @param color
     */
    public Series setMarkerColor(java.awt.Color color) {

        this.markerColor = color;
        return this;
    }

    public Collection<?> getXData() {

        return xData;
    }

    public AxisDataType getxAxisDataType() {

        return xAxisType;
    }

    public Collection<? extends Number> getYData() {

        return yData;
    }

    public AxisDataType getyAxisDataType() {

        return yAxisType;
    }

    public Collection<? extends Number> getErrorBars() {

        return errorBars;
    }

    public double getXMin() {

        return xMin;
    }

    public double getXMax() {

        return xMax;
    }

    public double getYMin() {

        return yMin;
    }

    public double getYMax() {

        return yMax;
    }

    public BasicStroke getLineStyle() {

        return stroke;
    }

    public Marker getMarker() {

        return marker;
    }

    public Color getLineColor() {

        return lineColor;
    }

    public Color getMarkerColor() {

        return markerColor;
    }

}
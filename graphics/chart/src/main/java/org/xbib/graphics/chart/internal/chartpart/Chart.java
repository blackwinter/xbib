package org.xbib.graphics.chart.internal.chartpart;

import org.xbib.graphics.chart.internal.Series;
import org.xbib.graphics.chart.internal.style.Styler;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * An XChart Chart
 */
public abstract class Chart<ST extends Styler, S extends Series> implements ChartPart {

    protected ST styler;
    /**
     * Chart Parts
     */
    protected ChartTitle chartTitle;
    protected Legend legend;
    protected Plot plot;
    protected AxisPair axisPair;
    protected Map<String, S> seriesMap = new LinkedHashMap<String, S>();
    /**
     * Meta Data
     */
    private int width;
    private int height;
    private String title = "";
    private String xAxisTitle = "";
    private String yAxisTitle = "";

    /**
     * Constructor
     *
     * @param width
     * @param height
     * @param styler
     */
    public Chart(int width, int height, ST styler) {

        this.width = width;
        this.height = height;
        this.styler = styler;

        this.chartTitle = new ChartTitle(this);
    }

    public abstract void paint(Graphics2D g, int width, int height);

    public List<Double> getNumberListFromDoubleArray(double[] data) {

        if (data == null) {
            return null;
        }

        List<Double> dataNumber = null;
        if (data != null) {
            dataNumber = new ArrayList<Double>();
            for (double d : data) {
                dataNumber.add(new Double(d));
            }
        }
        return dataNumber;
    }

    public List<Double> getNumberListFromIntArray(int[] data) {

        if (data == null) {
            return null;
        }

        List<Double> dataNumber = null;
        if (data != null) {
            dataNumber = new ArrayList<Double>();
            for (double d : data) {
                dataNumber.add(new Double(d));
            }
        }
        return dataNumber;
    }

    public List<Double> getGeneratedData(int length) {

        List<Double> generatedData = new ArrayList<Double>();
        for (int i = 1; i < length + 1; i++) {
            generatedData.add((double) i);
        }
        return generatedData;
    }

    /**
     * Meta Data Getters and Setters
     */
    public int getWidth() {

        return width;
    }

    public void setWidth(int width) {

        this.width = width;
    }

    public int getHeight() {

        return height;
    }

    public void setHeight(int height) {

        this.height = height;
    }

    public String getTitle() {

        return title;
    }

    public void setTitle(String title) {

        this.title = title;
    }

    public String getXAxisTitle() {

        return xAxisTitle;
    }

    public void setXAxisTitle(String xAxisTitle) {

        this.xAxisTitle = xAxisTitle;
    }

    public String getyYAxisTitle() {

        return yAxisTitle;
    }

    public void setYAxisTitle(String yAxisTitle) {

        this.yAxisTitle = yAxisTitle;
    }

    /**
     * Chart Parts Getters
     */

    protected ChartTitle getChartTitle() {

        return chartTitle;
    }

    protected Legend getLegend() {

        return legend;
    }

    protected Plot getPlot() {

        return plot;
    }

    protected Axis getXAxis() {

        return axisPair.getXAxis();
    }

    protected Axis getYAxis() {

        return axisPair.getYAxis();
    }

    public AxisPair getAxisPair() {

        return axisPair;
    }

    public Map<String, S> getSeriesMap() {

        return seriesMap;
    }

    public S removeSeries(String seriesName) {

        return seriesMap.remove(seriesName);
    }

    /**
     * Gets the Chart's styler, which can be used to customize the Chart's appearance
     *
     * @return the styler
     */
    public ST getStyler() {

        return styler;
    }

    @Override
    public Rectangle2D getBounds() {

        return new Rectangle2D.Double(0, 0, width, height);
    }
}

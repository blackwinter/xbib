package org.xbib.graphics.chart.internal.style.colors;

import java.awt.*;

public class XChartSeriesColors implements SeriesColors {

    // original XChart colors
    public static Color BLUE = new Color(0, 55, 255, 180);
    public static Color ORANGE = new Color(255, 172, 0, 180);
    public static Color PURPLE = new Color(128, 0, 255, 180);
    public static Color GREEN = new Color(0, 205, 0, 180);
    public static Color RED = new Color(205, 0, 0, 180);
    public static Color YELLOW = new Color(255, 215, 0, 180);
    public static Color MAGENTA = new Color(255, 0, 255, 180);
    public static Color PINK = new Color(255, 166, 201, 180);
    public static Color LIGHT_GREY = new Color(207, 207, 207, 180);
    public static Color CYAN = new Color(0, 255, 255, 180);
    public static Color BROWN = new Color(102, 56, 10, 180);
    public static Color BLACK = new Color(0, 0, 0, 180);

    private final Color[] seriesColors;

    /**
     * Constructor
     */
    public XChartSeriesColors() {

        seriesColors = new Color[]{BLUE, ORANGE, PURPLE, GREEN, RED, YELLOW, MAGENTA, PINK, LIGHT_GREY, CYAN, BROWN, BLACK};
    }

    @Override
    public Color[] getSeriesColors() {

        return seriesColors;
    }
}

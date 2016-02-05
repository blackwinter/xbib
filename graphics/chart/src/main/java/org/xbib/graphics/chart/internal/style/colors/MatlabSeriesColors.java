package org.xbib.graphics.chart.internal.style.colors;

import java.awt.*;

public class MatlabSeriesColors implements SeriesColors {

    public static Color BLUE = new Color(0, 0, 255, 255);
    public static Color GREEN = new Color(0, 128, 0, 255);
    public static Color RED = new Color(255, 0, 0, 255);
    public static Color TURQUOISE = new Color(0, 191, 191, 255);
    public static Color MAGENTA = new Color(191, 0, 191, 255);
    public static Color YELLOW = new Color(191, 191, 0, 255);
    public static Color DARK_GREY = new Color(64, 64, 64, 255);

    private final Color[] seriesColors;

    /**
     * Constructor
     */
    public MatlabSeriesColors() {

        seriesColors = new Color[]{BLUE, GREEN, RED, TURQUOISE, MAGENTA, YELLOW, DARK_GREY};
    }

    @Override
    public Color[] getSeriesColors() {

        return seriesColors;
    }
}

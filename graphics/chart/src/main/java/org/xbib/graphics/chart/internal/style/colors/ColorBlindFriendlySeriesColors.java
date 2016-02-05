package org.xbib.graphics.chart.internal.style.colors;

import java.awt.*;

public class ColorBlindFriendlySeriesColors implements SeriesColors {

    // The color blind friendly palette
    public static Color BLACK = new Color(0, 0, 0, 255);
    public static Color ORANGE = new Color(230, 159, 0, 255);
    public static Color SKY_BLUE = new Color(86, 180, 233, 255);
    public static Color BLUISH_GREEN = new Color(0, 158, 115, 255);
    public static Color YELLOW = new Color(240, 228, 66, 255);
    public static Color BLUE = new Color(0, 114, 178, 255);
    public static Color VERMILLION = new Color(213, 94, 0, 255);
    public static Color REDDISH_PURPLE = new Color(204, 121, 167, 255);

    private final Color[] seriesColors;

    /**
     * Constructor
     */
    public ColorBlindFriendlySeriesColors() {

        seriesColors = new Color[]{BLACK, ORANGE, SKY_BLUE, BLUISH_GREEN, YELLOW, BLUE, VERMILLION, REDDISH_PURPLE};
    }

    @Override
    public Color[] getSeriesColors() {

        return seriesColors;
    }
}

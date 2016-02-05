package org.xbib.graphics.chart.internal.style.lines;

import java.awt.*;

/**
 * Pre-defined Line Styles used for Series Lines
 */
public interface SeriesLines {

    public static BasicStroke NONE = new NoneStroke();
    public static BasicStroke SOLID = new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
    public static BasicStroke DASH_DOT = new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{3.0f, 1.0f}, 0.0f);
    public static BasicStroke DASH_DASH = new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{3.0f, 3.0f}, 0.0f);
    public static BasicStroke DOT_DOT = new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10.0f, new float[]{2.0f}, 0.0f);

    public BasicStroke[] getSeriesLines();

}

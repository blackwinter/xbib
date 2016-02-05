package org.xbib.graphics.chart.internal.chartpart;

import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * All components of a chart that need to be painted should implement this interface
 */
public interface ChartPart {

    Rectangle2D getBounds();

    void paint(Graphics2D g);

}

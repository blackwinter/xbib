package org.xbib.graphics.chart.internal.style.markers;

import java.awt.*;

public abstract class Marker {

    protected BasicStroke stroke = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);

    public abstract void paint(Graphics2D g, double xOffset, double yOffset, int markerSize);

}

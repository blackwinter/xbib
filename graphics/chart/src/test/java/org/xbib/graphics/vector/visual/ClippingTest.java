package org.xbib.graphics.vector.visual;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.io.IOException;

public class ClippingTest extends TestCase {
    public ClippingTest() throws IOException {
    }

    @Override
    public void draw(Graphics2D g) {
        double w = getPageSize().width;
        double h = getPageSize().height;

        AffineTransform txOrig = g.getTransform();
        g.translate(w / 2.0, h / 2.0);

        g.setClip(new Ellipse2D.Double(-0.6 * w / 2.0, -h / 2.0, 0.6 * w, h));
        for (double x = -w / 2.0; x < w / 2.0; x += 4.0) {
            g.draw(new Line2D.Double(x, -h / 2.0, x, h / 2.0));
        }

        g.rotate(Math.toRadians(-90.0));
        g.clip(new Ellipse2D.Double(-0.6 * w / 2.0, -h / 2.0, 0.6 * w, h));
        for (double x = -h / 2.0; x < h / 2.0; x += 4.0) {
            g.draw(new Line2D.Double(x, -w / 2.0, x, w / 2.0));
        }

        g.setTransform(txOrig);
        g.setClip(null);
        g.draw(new Line2D.Double(0.0, 0.0, w, h));
    }
}

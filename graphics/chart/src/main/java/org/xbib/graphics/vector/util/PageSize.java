package org.xbib.graphics.vector.util;

import java.awt.geom.Rectangle2D;

public class PageSize {
    private static final double MM_PER_INCH = 2.54;
    public static final PageSize TABLOID = new PageSize(11.0 * MM_PER_INCH, 17.0 * MM_PER_INCH);
    public static final PageSize LETTER = new PageSize(8.5 * MM_PER_INCH, 11.0 * MM_PER_INCH);
    public static final PageSize LEGAL = new PageSize(8.5 * MM_PER_INCH, 14.0 * MM_PER_INCH);
    public static final PageSize LEDGER = TABLOID.getLandscape();
    public static final PageSize A3 = new PageSize(297.0, 420.0);
    public static final PageSize A4 = new PageSize(210.0, 297.0);
    public static final PageSize A5 = new PageSize(148.0, 210.0);
    public final double x;
    public final double y;
    public final double width;
    public final double height;

    public PageSize(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public PageSize(double width, double height) {
        this(0.0, 0.0, width, height);
    }

    public PageSize(Rectangle2D size) {
        this(size.getX(), size.getY(), size.getWidth(), size.getHeight());
    }

    public PageSize getPortrait() {
        if (width <= height) {
            return this;
        }
        return new PageSize(x, y, height, width);
    }

    public PageSize getLandscape() {
        if (width >= height) {
            return this;
        }
        return new PageSize(x, y, height, width);
    }
}


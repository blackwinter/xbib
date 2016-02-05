package org.xbib.graphics.vector.intermediate.commands;

import java.awt.geom.AffineTransform;
import java.util.Locale;

public class TranslateCommand extends AffineTransformCommand {
    private final double deltaX;
    private final double deltaY;

    public TranslateCommand(double x, double y) {
        super(AffineTransform.getTranslateInstance(x, y));
        this.deltaX = x;
        this.deltaY = y;
    }

    public double getDeltaX() {
        return deltaX;
    }

    public double getDeltaY() {
        return deltaY;
    }

    @Override
    public String toString() {
        return String.format((Locale) null,
                "%s[deltaX=%f, deltaY=%f, value=%s]", getClass().getName(),
                getDeltaX(), getDeltaY(), getValue());
    }
}


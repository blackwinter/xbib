package org.xbib.graphics.vector.intermediate.commands;

import java.awt.geom.AffineTransform;
import java.util.Locale;

public class ScaleCommand extends AffineTransformCommand {
    private final double scaleX;
    private final double scaleY;

    public ScaleCommand(double scaleX, double scaleY) {
        super(AffineTransform.getScaleInstance(scaleX, scaleY));
        this.scaleX = scaleX;
        this.scaleY = scaleY;
    }

    public double getScaleX() {
        return scaleX;
    }

    public double getScaleY() {
        return scaleY;
    }

    @Override
    public String toString() {
        return String.format((Locale) null,
                "%s[scaleX=%f, scaleY=%f, value=%s]", getClass().getName(),
                getScaleX(), getScaleY(), getValue());
    }
}


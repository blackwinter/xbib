package org.xbib.graphics.vector.intermediate.commands;

import java.awt.geom.AffineTransform;
import java.util.Locale;

public class ShearCommand extends AffineTransformCommand {
    private final double shearX;
    private final double shearY;

    public ShearCommand(double shearX, double shearY) {
        super(AffineTransform.getShearInstance(shearX, shearY));
        this.shearX = shearX;
        this.shearY = shearY;
    }

    public double getShearX() {
        return shearX;
    }

    public double getShearY() {
        return shearY;
    }

    @Override
    public String toString() {
        return String.format((Locale) null,
                "%s[shearX=%f, shearY=%f, value=%s]", getClass().getName(),
                getShearX(), getShearY(), getValue());
    }
}


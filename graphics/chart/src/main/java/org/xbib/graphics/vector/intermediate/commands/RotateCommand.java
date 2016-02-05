package org.xbib.graphics.vector.intermediate.commands;

import java.awt.geom.AffineTransform;
import java.util.Locale;

public class RotateCommand extends AffineTransformCommand {
    private final double theta;
    private final double centerX;
    private final double centerY;

    public RotateCommand(double theta, double centerX, double centerY) {
        super(AffineTransform.getRotateInstance(theta, centerX, centerY));
        this.theta = theta;
        this.centerX = centerX;
        this.centerY = centerY;
    }

    public double getTheta() {
        return theta;
    }

    public double getCenterX() {
        return centerX;
    }

    public double getCenterY() {
        return centerY;
    }

    @Override
    public String toString() {
        return String.format((Locale) null,
                "%s[theta=%f, centerX=%f, centerY=%f, value=%s]",
                getClass().getName(), getTheta(), getCenterX(), getCenterY(),
                getValue());
    }
}


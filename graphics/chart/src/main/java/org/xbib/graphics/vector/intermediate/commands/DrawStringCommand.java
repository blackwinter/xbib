package org.xbib.graphics.vector.intermediate.commands;

import java.util.Locale;


public class DrawStringCommand extends Command<String> {
    private final double x;
    private final double y;

    public DrawStringCommand(String string, double x, double y) {
        super(string);
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    @Override
    public String toString() {
        return String.format((Locale) null, "%s[value=%s, x=%f, y=%f]",
                getClass().getName(), getValue(), getX(), getY());
    }
}


package org.xbib.graphics.vector.intermediate.commands;

import org.xbib.graphics.vector.util.GraphicsUtils;

import java.awt.*;

public class DrawShapeCommand extends Command<Shape> {
    public DrawShapeCommand(Shape shape) {
        super(GraphicsUtils.clone(shape));
    }
}


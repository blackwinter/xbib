package org.xbib.graphics.vector.intermediate.commands;

import org.xbib.graphics.vector.util.GraphicsUtils;

import java.awt.*;

public class FillShapeCommand extends Command<Shape> {
    public FillShapeCommand(Shape shape) {
        super(GraphicsUtils.clone(shape));
    }
}


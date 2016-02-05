package org.xbib.graphics.vector.intermediate.commands;

import java.awt.*;

public class SetClipCommand extends StateCommand<Shape> {
    public SetClipCommand(Shape shape) {
        super(shape);
    }
}


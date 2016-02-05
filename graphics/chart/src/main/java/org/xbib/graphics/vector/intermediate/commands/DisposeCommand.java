package org.xbib.graphics.vector.intermediate.commands;

import org.xbib.graphics.vector.VectorGraphics2D;

public class DisposeCommand extends StateCommand<VectorGraphics2D> {
    public DisposeCommand(VectorGraphics2D graphics) {
        super(graphics);
    }
}


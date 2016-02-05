package org.xbib.graphics.vector.intermediate.commands;

import java.awt.*;

public class SetStrokeCommand extends StateCommand<Stroke> {
    public SetStrokeCommand(Stroke stroke) {
        super(stroke);
    }
}


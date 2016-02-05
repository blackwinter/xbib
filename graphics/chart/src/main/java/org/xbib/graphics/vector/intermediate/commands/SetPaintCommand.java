package org.xbib.graphics.vector.intermediate.commands;

import java.awt.*;

public class SetPaintCommand extends StateCommand<Paint> {
    public SetPaintCommand(Paint paint) {
        super(paint);
    }
}


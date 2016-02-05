package org.xbib.graphics.vector.intermediate.commands;

import java.awt.*;

public class SetBackgroundCommand extends StateCommand<Color> {
    public SetBackgroundCommand(Color color) {
        super(color);
    }
}


package org.xbib.graphics.vector.intermediate.commands;

import java.awt.*;

public class SetColorCommand extends StateCommand<Color> {
    public SetColorCommand(Color color) {
        super(color);
    }
}


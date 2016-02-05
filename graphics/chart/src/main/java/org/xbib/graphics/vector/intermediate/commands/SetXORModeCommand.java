package org.xbib.graphics.vector.intermediate.commands;

import java.awt.*;

public class SetXORModeCommand extends StateCommand<Color> {
    public SetXORModeCommand(Color mode) {
        super(mode);
    }
}


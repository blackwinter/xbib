package org.xbib.graphics.vector.intermediate.commands;

import java.awt.*;

public class SetFontCommand extends StateCommand<Font> {
    public SetFontCommand(Font font) {
        super(font);
    }
}


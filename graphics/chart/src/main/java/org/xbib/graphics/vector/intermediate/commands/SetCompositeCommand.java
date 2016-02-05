package org.xbib.graphics.vector.intermediate.commands;

import java.awt.*;

public class SetCompositeCommand extends StateCommand<Composite> {
    public SetCompositeCommand(Composite composite) {
        super(composite);
    }
}


package org.xbib.graphics.vector.intermediate.commands;

import java.util.LinkedList;
import java.util.List;

public class Group extends Command<List<Command<?>>> {
    public Group() {
        super(new LinkedList<Command<?>>());
    }

    public void add(Command<?> command) {
        List<Command<?>> group = getValue();
        group.add(command);
    }
}


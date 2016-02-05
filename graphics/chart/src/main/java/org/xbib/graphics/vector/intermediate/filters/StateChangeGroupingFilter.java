package org.xbib.graphics.vector.intermediate.filters;

import org.xbib.graphics.vector.intermediate.commands.Command;
import org.xbib.graphics.vector.intermediate.commands.StateCommand;


public class StateChangeGroupingFilter extends GroupingFilter {

    public StateChangeGroupingFilter(Iterable<Command<?>> stream) {
        super(stream);
    }

    @Override
    protected boolean isGrouped(Command<?> command) {
        return command instanceof StateCommand;
    }
}


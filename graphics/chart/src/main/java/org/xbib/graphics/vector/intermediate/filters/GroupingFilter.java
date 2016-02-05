package org.xbib.graphics.vector.intermediate.filters;

import org.xbib.graphics.vector.intermediate.commands.Command;
import org.xbib.graphics.vector.intermediate.commands.Group;

import java.util.Arrays;
import java.util.List;


public abstract class GroupingFilter extends Filter {
    private Group group;

    public GroupingFilter(Iterable<Command<?>> stream) {
        super(stream);
    }

    @Override
    public boolean hasNext() {
        return group != null || super.hasNext();
    }

    @Override
    public Command<?> next() {
        if (group == null) {
            return super.next();
        }
        Group g = group;
        group = null;
        return g;
    }

    @Override
    protected List<Command<?>> filter(Command<?> command) {
        boolean grouped = isGrouped(command);
        if (grouped) {
            if (group == null) {
                group = new Group();
            }
            group.add(command);
            return null;
        }
        return Arrays.<Command<?>>asList(command);
    }

    protected abstract boolean isGrouped(Command<?> command);
}


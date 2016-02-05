package org.xbib.graphics.vector.intermediate.filters;

import org.xbib.graphics.vector.intermediate.commands.AffineTransformCommand;
import org.xbib.graphics.vector.intermediate.commands.Command;
import org.xbib.graphics.vector.intermediate.commands.SetHintCommand;
import org.xbib.graphics.vector.intermediate.commands.StateCommand;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class OptimizeFilter extends Filter {
    private final Queue<Command<?>> buffer;

    public OptimizeFilter(Iterable<Command<?>> stream) {
        super(stream);
        buffer = new LinkedList<Command<?>>();
    }

    private static boolean isStateChange(Command<?> command) {
        return (command instanceof StateCommand) &&
                !(command instanceof AffineTransformCommand) &&
                !(command instanceof SetHintCommand);
    }

    @Override
    public boolean hasNext() {
        return super.hasNext();
    }

    @Override
    public Command<?> next() {
        if (buffer.isEmpty()) {
            return super.next();
        }
        return buffer.poll();
    }

    @Override
    protected List<Command<?>> filter(Command<?> command) {
        if (!isStateChange(command)) {
            return Arrays.<Command<?>>asList(command);
        }
        Iterator<Command<?>> i = buffer.iterator();
        Class<?> cls = command.getClass();
        while (i.hasNext()) {
            if (cls.equals(i.next().getClass())) {
                i.remove();
            }
        }
        buffer.add(command);
        return null;
    }
}


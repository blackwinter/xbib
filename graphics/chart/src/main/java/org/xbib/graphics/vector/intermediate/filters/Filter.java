package org.xbib.graphics.vector.intermediate.filters;

import org.xbib.graphics.vector.intermediate.commands.Command;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public abstract class Filter implements Iterable<Command<?>>, Iterator<Command<?>> {
    private final Queue<Command<?>> buffer;
    private final Iterator<Command<?>> iterator;

    public Filter(Iterable<Command<?>> stream) {
        buffer = new LinkedList<Command<?>>();
        iterator = stream.iterator();
    }

    public Iterator<Command<?>> iterator() {
        return this;
    }

    public boolean hasNext() {
        findNextCommand();
        return !buffer.isEmpty();
    }

    private void findNextCommand() {
        while (buffer.isEmpty() && iterator.hasNext()) {
            Command<?> command = iterator.next();
            List<Command<?>> commands = filter(command);
            if (commands != null) {
                buffer.addAll(commands);
            }
        }
    }

    public Command<?> next() {
        findNextCommand();
        return buffer.poll();
    }

    public void remove() {
    }

    protected abstract List<Command<?>> filter(Command<?> command);
}


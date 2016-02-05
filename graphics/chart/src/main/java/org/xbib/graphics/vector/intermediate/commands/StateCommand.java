package org.xbib.graphics.vector.intermediate.commands;

public abstract class StateCommand<T> extends Command<T> {
    public StateCommand(T value) {
        super(value);
    }
}


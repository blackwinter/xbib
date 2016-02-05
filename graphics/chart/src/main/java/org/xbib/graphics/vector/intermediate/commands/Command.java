package org.xbib.graphics.vector.intermediate.commands;

import java.util.Locale;

public abstract class Command<T> {
    private final T value;

    public Command(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.format((Locale) null, "%s[value=%s]",
                getClass().getName(), getValue());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !getClass().equals(obj.getClass())) {
            return false;
        }
        Command<?> o = (Command<?>) obj;
        return value == o.value || value.equals(o.value);
    }
}


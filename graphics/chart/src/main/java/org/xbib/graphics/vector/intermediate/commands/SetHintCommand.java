package org.xbib.graphics.vector.intermediate.commands;

import java.util.Locale;

public class SetHintCommand extends StateCommand<Object> {
    private final Object key;

    public SetHintCommand(Object hintKey, Object hintValue) {
        super(hintValue);
        key = hintKey;
    }

    public Object getKey() {
        return key;
    }

    @Override
    public String toString() {
        return String.format((Locale) null,
                "%s[key=%s, value=%s]", getClass().getName(),
                getKey(), getValue());
    }
}


package org.xbib.time.chronic.handlers;

public class HandlerTypePattern extends HandlerPattern {
    private Handler.HandlerType type;

    public HandlerTypePattern(Handler.HandlerType type) {
        this(type, false);
    }

    public HandlerTypePattern(Handler.HandlerType type, boolean optional) {
        super(optional);
        this.type = type;
    }

    public Handler.HandlerType getType() {
        return type;
    }
}

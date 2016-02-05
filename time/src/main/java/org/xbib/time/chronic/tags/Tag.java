package org.xbib.time.chronic.tags;

import java.util.Calendar;

/**
 * Tokens are tagged with subclassed instances of this class when
 * they match specific criteria
 */
public class Tag<T> {
    private T type;
    private Calendar now;

    public Tag(T type) {
        this.type = type;
    }

    public Calendar getNow() {
        return now;
    }

    public T getType() {
        return type;
    }

    public void setType(T type) {
        this.type = type;
    }

    public void setStart(Calendar s) {
        now = s;
    }
}

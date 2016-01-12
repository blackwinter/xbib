package org.xbib.time.chronic.repeaters;

import org.xbib.time.chronic.Span;
import org.xbib.time.chronic.Time;
import org.xbib.time.chronic.tags.Pointer.PointerType;

import java.util.Calendar;

public class RepeaterSecond extends RepeaterUnit {
    public static final int SECOND_SECONDS = 1; // (60 * 60);

    private Calendar secondStart;

    @Override
    protected Span _nextSpan(PointerType pointer) {
        int direction = (pointer == PointerType.FUTURE) ? 1 : -1;
        if (secondStart == null) {
            secondStart = Time.cloneAndAdd(getNow(), Calendar.SECOND, direction);
        } else {
            secondStart.add(Calendar.SECOND, direction);
        }

        return new Span(secondStart, Calendar.SECOND, 1);
    }

    @Override
    protected Span _thisSpan(PointerType pointer) {
        return new Span(getNow(), Calendar.SECOND, 1);
    }

    @Override
    public Span getOffset(Span span, int amount, PointerType pointer) {
        int direction = pointer == PointerType.FUTURE ? 1 : -1;
        // WARN: Does not use Calendar
        return span.add(direction * amount * RepeaterSecond.SECOND_SECONDS);
    }

    @Override
    public int getWidth() {
        // WARN: Does not use Calendar
        return RepeaterSecond.SECOND_SECONDS;
    }

    @Override
    public String toString() {
        return super.toString() + "-second";
    }
}

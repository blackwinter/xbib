package org.xbib.time.chronic.repeaters;

import org.xbib.time.chronic.Span;
import org.xbib.time.chronic.Time;
import org.xbib.time.chronic.tags.Pointer.PointerType;

import java.util.Calendar;

public class RepeaterMinute extends RepeaterUnit {
    public static final int MINUTE_SECONDS = 60;

    private Calendar currentMinuteStart;

    @Override
    protected Span _nextSpan(PointerType pointer) {
        if (currentMinuteStart == null) {
            if (pointer == PointerType.FUTURE) {
                currentMinuteStart = Time.cloneAndAdd(Time.ymdhm(getNow()), Calendar.MINUTE, 1);
            } else if (pointer == PointerType.PAST) {
                currentMinuteStart = Time.cloneAndAdd(Time.ymdhm(getNow()), Calendar.MINUTE, -1);
            } else {
                throw new IllegalArgumentException("Unable to handle pointer " + pointer + ".");
            }
        } else {
            int direction = (pointer == PointerType.FUTURE) ? 1 : -1;
            currentMinuteStart.add(Calendar.MINUTE, direction);
        }

        return new Span(currentMinuteStart, Calendar.SECOND, RepeaterMinute.MINUTE_SECONDS);
    }

    @Override
    protected Span _thisSpan(PointerType pointer) {
        Calendar minuteBegin;
        Calendar minuteEnd;
        if (pointer == PointerType.FUTURE) {
            minuteBegin = getNow();
            minuteEnd = Time.ymdhm(getNow());
        } else if (pointer == PointerType.PAST) {
            minuteBegin = Time.ymdhm(getNow());
            minuteEnd = getNow();
        } else if (pointer == PointerType.NONE) {
            minuteBegin = Time.ymdhm(getNow());
            minuteEnd = Time.cloneAndAdd(Time.ymdhm(getNow()), Calendar.SECOND, RepeaterMinute.MINUTE_SECONDS);
        } else {
            throw new IllegalArgumentException("Unable to handle pointer " + pointer + ".");
        }
        return new Span(minuteBegin, minuteEnd);
    }

    @Override
    public Span getOffset(Span span, int amount, PointerType pointer) {
        int direction = (pointer == PointerType.FUTURE) ? 1 : -1;
        // WARN: Does not use Calendar
        return span.add(direction * amount * RepeaterMinute.MINUTE_SECONDS);
    }

    @Override
    public int getWidth() {
        // WARN: Does not use Calendar
        return RepeaterMinute.MINUTE_SECONDS;
    }

    @Override
    public String toString() {
        return super.toString() + "-minute";
    }
}

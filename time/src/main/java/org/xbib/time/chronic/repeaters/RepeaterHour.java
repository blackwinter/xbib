package org.xbib.time.chronic.repeaters;

import org.xbib.time.chronic.Span;
import org.xbib.time.chronic.Time;
import org.xbib.time.chronic.tags.Pointer.PointerType;

import java.util.Calendar;

public class RepeaterHour extends RepeaterUnit {
    public static final int HOUR_SECONDS = 3600; // (60 * 60);

    private Calendar currentDayStart;

    @Override
    protected Span _nextSpan(PointerType pointer) {
        if (currentDayStart == null) {
            if (pointer == PointerType.FUTURE) {
                currentDayStart = Time.cloneAndAdd(Time.ymdh(getNow()), Calendar.HOUR, 1);
            } else if (pointer == PointerType.PAST) {
                currentDayStart = Time.cloneAndAdd(Time.ymdh(getNow()), Calendar.HOUR, -1);
            } else {
                throw new IllegalArgumentException("Unable to handle pointer " + pointer + ".");
            }
        } else {
            int direction = (pointer == PointerType.FUTURE) ? 1 : -1;
            currentDayStart.add(Calendar.HOUR, direction);
        }
        return new Span(currentDayStart, Calendar.HOUR, 1);
    }

    @Override
    protected Span _thisSpan(PointerType pointer) {
        Calendar hourStart;
        Calendar hourEnd;
        if (pointer == PointerType.FUTURE) {
            hourStart = Time.cloneAndAdd(Time.ymdhm(getNow()), Calendar.MINUTE, 1);
            hourEnd = Time.cloneAndAdd(Time.ymdh(getNow()), Calendar.HOUR, 1);
        } else if (pointer == PointerType.PAST) {
            hourStart = Time.ymdh(getNow());
            hourEnd = Time.ymdhm(getNow());
        } else if (pointer == PointerType.NONE) {
            hourStart = Time.ymdh(getNow());
            hourEnd = Time.cloneAndAdd(hourStart, Calendar.HOUR, 1);
        } else {
            throw new IllegalArgumentException("Unable to handle pointer " + pointer + ".");
        }
        return new Span(hourStart, hourEnd);
    }

    @Override
    public Span getOffset(Span span, int amount, PointerType pointer) {
        int direction = (pointer == PointerType.FUTURE) ? 1 : -1;
        // WARN: Does not use Calendar
        return span.add(direction * amount * RepeaterHour.HOUR_SECONDS);
    }

    @Override
    public int getWidth() {
        // WARN: Does not use Calendar
        return RepeaterHour.HOUR_SECONDS;
    }

    @Override
    public String toString() {
        return super.toString() + "-hour";
    }
}

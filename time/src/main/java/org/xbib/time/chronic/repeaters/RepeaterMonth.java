package org.xbib.time.chronic.repeaters;

import org.xbib.time.chronic.Span;
import org.xbib.time.chronic.Time;
import org.xbib.time.chronic.tags.Pointer.PointerType;

import java.util.Calendar;

public class RepeaterMonth extends RepeaterUnit {
    private static final int MONTH_SECONDS = 2592000; // 30 * 24 * 60 * 60

    private Calendar currentMonthStart;

    @Override
    protected Span _nextSpan(PointerType pointer) {
        int direction = (pointer == PointerType.FUTURE) ? 1 : -1;
        if (currentMonthStart == null) {
            currentMonthStart = Time.cloneAndAdd(Time.ym(getNow()), Calendar.MONTH, direction);
        } else {
            currentMonthStart = Time.cloneAndAdd(currentMonthStart, Calendar.MONTH, direction);
        }

        return new Span(currentMonthStart, Calendar.MONTH, 1);
    }

    @Override
    public Span getOffset(Span span, int amount, PointerType pointer) {
        long l = amount * (pointer == PointerType.FUTURE ? 1L : -1L);
        return new Span(Time.cloneAndAdd(span.getBeginCalendar(), Calendar.MONTH, l), Time.cloneAndAdd(span.getEndCalendar(), Calendar.MONTH, l));
    }

    @Override
    protected Span _thisSpan(PointerType pointer) {
        Calendar monthStart;
        Calendar monthEnd;
        if (pointer == PointerType.FUTURE) {
            monthStart = Time.cloneAndAdd(Time.ymd(getNow()), Calendar.DAY_OF_MONTH, 1);
            monthEnd = Time.cloneAndAdd(Time.ym(getNow()), Calendar.MONTH, 1);
        } else if (pointer == PointerType.PAST) {
            monthStart = Time.ym(getNow());
            monthEnd = Time.ymd(getNow());
        } else if (pointer == PointerType.NONE) {
            monthStart = Time.ym(getNow());
            monthEnd = Time.cloneAndAdd(Time.ym(getNow()), Calendar.MONTH, 1);
        } else {
            throw new IllegalArgumentException("Unable to handle pointer " + pointer + ".");
        }
        return new Span(monthStart, monthEnd);
    }

    @Override
    public int getWidth() {
        // WARN: Does not use Calendar
        return RepeaterMonth.MONTH_SECONDS;
    }

    @Override
    public String toString() {
        return super.toString() + "-month";
    }
}
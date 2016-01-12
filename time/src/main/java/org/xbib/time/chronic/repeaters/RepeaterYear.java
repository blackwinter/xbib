package org.xbib.time.chronic.repeaters;

import org.xbib.time.chronic.Span;
import org.xbib.time.chronic.Time;
import org.xbib.time.chronic.tags.Pointer.PointerType;

import java.util.Calendar;

public class RepeaterYear extends RepeaterUnit {
    private Calendar currentYearStart;

    @Override
    protected Span _nextSpan(PointerType pointer) {
        if (currentYearStart == null) {
            if (pointer == PointerType.FUTURE) {
                currentYearStart = Time.cloneAndAdd(Time.y(getNow()), Calendar.YEAR, 1);
            } else if (pointer == PointerType.PAST) {
                currentYearStart = Time.cloneAndAdd(Time.y(getNow()), Calendar.YEAR, -1);
            } else {
                throw new IllegalArgumentException("Unable to handle pointer " + pointer + ".");
            }
        } else {
            int direction = (pointer == PointerType.FUTURE) ? 1 : -1;
            currentYearStart.add(Calendar.YEAR, direction);
        }

        return new Span(currentYearStart, Calendar.YEAR, 1);
    }

    @Override
    protected Span _thisSpan(PointerType pointer) {
        Calendar yearStart;
        Calendar yearEnd;
        if (pointer == PointerType.FUTURE) {
            yearStart = Time.cloneAndAdd(Time.ymd(getNow()), Calendar.DAY_OF_MONTH, 1);
            yearEnd = Time.cloneAndAdd(Time.yJan1(getNow()), Calendar.YEAR, 1);
        } else if (pointer == PointerType.PAST) {
            yearStart = Time.yJan1(getNow());
            yearEnd = Time.ymd(getNow());
        } else if (pointer == PointerType.NONE) {
            yearStart = Time.yJan1(getNow());
            yearEnd = Time.cloneAndAdd(Time.yJan1(getNow()), Calendar.YEAR, 1);
        } else {
            throw new IllegalArgumentException("Unable to handle pointer " + pointer + ".");
        }
        return new Span(yearStart, yearEnd);
    }

    @Override
    public Span getOffset(Span span, int amount, PointerType pointer) {
        int direction = (pointer == PointerType.FUTURE) ? 1 : -1;
        Calendar newBegin = Time.cloneAndAdd(span.getBeginCalendar(), Calendar.YEAR, amount * direction);
        Calendar newEnd = Time.cloneAndAdd(span.getEndCalendar(), Calendar.YEAR, amount * direction);
        return new Span(newBegin, newEnd);
    }

    @Override
    public int getWidth() {
        // WARN: Does not use Calendar
        return (365 * 24 * 60 * 60);
    }

    @Override
    public String toString() {
        return super.toString() + "-year";
    }
}

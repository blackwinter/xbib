package org.xbib.time.chronic.repeaters;

import org.xbib.time.chronic.Span;
import org.xbib.time.chronic.tags.Pointer.PointerType;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

public class RepeaterDay extends RepeaterUnit {
    public static final int DAY_SECONDS = 86400;

    private ZonedDateTime currentDayStart;

    @Override
    protected Span _nextSpan(PointerType pointer) {
        if (currentDayStart == null) {
            currentDayStart = ymd(getNow());
        }
        int direction = pointer == PointerType.FUTURE ? 1 : -1;
        currentDayStart = currentDayStart.plus(direction, ChronoUnit.DAYS);
        return new Span(currentDayStart, ChronoUnit.DAYS, 1);
    }

    @Override
    protected Span _thisSpan(PointerType pointer) {
        ZonedDateTime dayBegin;
        ZonedDateTime dayEnd;
        if (pointer == PointerType.FUTURE) {
            dayBegin = ymdh(getNow()).plus(1, ChronoUnit.HOURS);
            dayEnd = ymd(getNow()).plus(1, ChronoUnit.DAYS);
        } else if (pointer == PointerType.PAST) {
            dayBegin = ymd(getNow());
            dayEnd = ymdh(getNow());
        } else if (pointer == PointerType.NONE) {
            dayBegin = ymd(getNow());
            dayEnd = ymdh(getNow()).plus(1, ChronoUnit.DAYS);
        } else {
            throw new IllegalArgumentException("unable to handle pointer " + pointer + ".");
        }
        return new Span(dayBegin, dayEnd);
    }

    @Override
    public Span getOffset(Span span, int amount, PointerType pointer) {
        long direction = pointer == PointerType.FUTURE ? 1L : -1L;
        return span.add(direction * amount * RepeaterDay.DAY_SECONDS);
    }

    @Override
    public int getWidth() {
        return RepeaterDay.DAY_SECONDS;
    }

    @Override
    public String toString() {
        return super.toString() + "-day";
    }

    private static ZonedDateTime ymd(ZonedDateTime zonedDateTime) {
        return ZonedDateTime.of(zonedDateTime.getYear(), zonedDateTime.getMonthValue(), zonedDateTime.getDayOfMonth(),
                0, 0, 0, 0, zonedDateTime.getZone());
    }

    private static ZonedDateTime ymdh(ZonedDateTime zonedDateTime) {
        return ZonedDateTime.of(zonedDateTime.getYear(), zonedDateTime.getMonthValue(), zonedDateTime.getDayOfMonth(),
                zonedDateTime.getHour(), 0, 0, 0, zonedDateTime.getZone());
    }

}

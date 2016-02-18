package org.xbib.time.chronic.repeaters;

import org.xbib.time.chronic.Span;
import org.xbib.time.chronic.tags.Pointer.PointerType;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

public class RepeaterYear extends RepeaterUnit {
    private ZonedDateTime currentYearStart;

    @Override
    protected Span _nextSpan(PointerType pointer) {
        if (currentYearStart == null) {
            if (pointer == PointerType.FUTURE) {
                currentYearStart = ZonedDateTime.of(getNow().getYear(), 1, 1, 0, 0, 0, 0, getNow().getZone())
                        .plus(1, ChronoUnit.YEARS);
            } else if (pointer == PointerType.PAST) {
                currentYearStart = ZonedDateTime.of(getNow().getYear(), 1, 1, 0, 0, 0, 0, getNow().getZone())
                        .minus(1, ChronoUnit.YEARS);
            } else {
                throw new IllegalArgumentException("Unable to handle pointer " + pointer + ".");
            }
        } else {
            int direction = (pointer == PointerType.FUTURE) ? 1 : -1;
            currentYearStart = currentYearStart.plus(direction, ChronoUnit.YEARS);
        }
        return new Span(currentYearStart, ChronoUnit.YEARS, 1);
    }

    @Override
    protected Span _thisSpan(PointerType pointer) {
        ZonedDateTime yearStart;
        ZonedDateTime yearEnd;
        if (pointer == PointerType.FUTURE) {
            yearStart = ymd(getNow()).plus(1, ChronoUnit.DAYS);
            yearEnd = ZonedDateTime.of(getNow().getYear(), 1, 1, 0, 0, 0, 0, getNow().getZone())
                    .plus(1, ChronoUnit.YEARS);
        } else if (pointer == PointerType.PAST) {
            yearStart = ZonedDateTime.of(getNow().getYear(), 1, 1, 0, 0, 0, 0, getNow().getZone());
            yearEnd = ymd(getNow());
        } else if (pointer == PointerType.NONE) {
            yearStart = ZonedDateTime.of(getNow().getYear(), 1, 1, 0, 0, 0, 0, getNow().getZone());
            yearEnd = ZonedDateTime.of(getNow().getYear(), 1, 1, 0, 0, 0, 0, getNow().getZone())
                    .plus(1, ChronoUnit.YEARS);
        } else {
            throw new IllegalArgumentException("Unable to handle pointer " + pointer + ".");
        }
        return new Span(yearStart, yearEnd);
    }

    @Override
    public Span getOffset(Span span, int amount, PointerType pointer) {
        long l = amount * (pointer == PointerType.FUTURE ? 1L : -1L);
        ZonedDateTime newBegin = span.getBeginCalendar().plus(l, ChronoUnit.YEARS);
        ZonedDateTime newEnd = span.getEndCalendar().plus(l, ChronoUnit.YEARS);
        return new Span(newBegin, newEnd);
    }

    @Override
    public int getWidth() {
        return (365 * 24 * 60 * 60);
    }

    @Override
    public String toString() {
        return super.toString() + "-year";
    }

    private static ZonedDateTime ymd(ZonedDateTime zonedDateTime) {
        return ZonedDateTime.of(zonedDateTime.getYear(), zonedDateTime.getMonthValue(), zonedDateTime.getDayOfMonth(),
                0, 0, 0, 0, zonedDateTime.getZone());
    }
}

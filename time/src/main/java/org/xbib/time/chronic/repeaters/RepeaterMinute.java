package org.xbib.time.chronic.repeaters;

import org.xbib.time.chronic.Span;
import org.xbib.time.chronic.tags.Pointer.PointerType;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

public class RepeaterMinute extends RepeaterUnit {
    public static final int MINUTE_SECONDS = 60;

    private ZonedDateTime currentMinuteStart;

    @Override
    protected Span _nextSpan(PointerType pointer) {
        if (currentMinuteStart == null) {
            if (pointer == PointerType.FUTURE) {
                currentMinuteStart = ymdhm(getNow()).plus(1, ChronoUnit.MINUTES);
            } else if (pointer == PointerType.PAST) {
                currentMinuteStart = ymdhm(getNow()).minus(1, ChronoUnit.MINUTES);
            } else {
                throw new IllegalArgumentException("Unable to handle pointer " + pointer + ".");
            }
        } else {
            int direction = pointer == PointerType.FUTURE ? 1 : -1;
            currentMinuteStart = currentMinuteStart.plus(direction, ChronoUnit.MINUTES);
        }
        return new Span(currentMinuteStart, ChronoUnit.SECONDS, RepeaterMinute.MINUTE_SECONDS);
    }

    @Override
    protected Span _thisSpan(PointerType pointer) {
        ZonedDateTime minuteBegin;
        ZonedDateTime minuteEnd;
        if (pointer == PointerType.FUTURE) {
            minuteBegin = getNow();
            minuteEnd = ymdhm(getNow());
        } else if (pointer == PointerType.PAST) {
            minuteBegin = ymdhm(getNow());
            minuteEnd = getNow();
        } else if (pointer == PointerType.NONE) {
            minuteBegin = ymdhm(getNow());
            minuteEnd = ymdhm(getNow()).plus(RepeaterMinute.MINUTE_SECONDS, ChronoUnit.SECONDS);
        } else {
            throw new IllegalArgumentException("Unable to handle pointer " + pointer + ".");
        }
        return new Span(minuteBegin, minuteEnd);
    }

    @Override
    public Span getOffset(Span span, int amount, PointerType pointer) {
        long direction = pointer == PointerType.FUTURE ? 1L : -1L;
        return span.add(direction * amount * RepeaterMinute.MINUTE_SECONDS);
    }

    @Override
    public int getWidth() {
        return RepeaterMinute.MINUTE_SECONDS;
    }

    @Override
    public String toString() {
        return super.toString() + "-minute";
    }

    private static ZonedDateTime ymdhm(ZonedDateTime zonedDateTime) {
        return ZonedDateTime.of(zonedDateTime.getYear(), zonedDateTime.getMonthValue(), zonedDateTime.getDayOfMonth(),
                zonedDateTime.getHour(), zonedDateTime.getMinute(), 0, 0, zonedDateTime.getZone());
    }
}

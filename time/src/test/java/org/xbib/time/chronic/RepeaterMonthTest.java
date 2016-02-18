package org.xbib.time.chronic;

import org.junit.Assert;
import org.junit.Test;
import org.xbib.time.chronic.repeaters.RepeaterMonth;
import org.xbib.time.chronic.tags.Pointer;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

public class RepeaterMonthTest extends Assert {

    private ZonedDateTime now = construct(2006, 8, 16, 14, 0, 0);

    private final static ZoneId ZONE_ID = ZoneId.of("GMT");

    public static ZonedDateTime construct(int year, int month, int day, int hour) {
        return ZonedDateTime.of(year, month, day, hour, 0, 0, 0, ZONE_ID);
    }

    public static ZonedDateTime construct(int year, int month, int day, int hour, int minutes) {
        return ZonedDateTime.of(year, month, day, hour, minutes, 0, 0, ZONE_ID);
    }

    public static ZonedDateTime construct(int year, int month, int day, int hour, int minutes, int seconds) {
        return ZonedDateTime.of(year, month, day, hour, minutes, seconds, 0, ZONE_ID);
    }

    @Test
    public void testOffset() {
        Span span = new Span(now, ChronoUnit.SECONDS, 60);

        Span offsetSpan;
        offsetSpan = new RepeaterMonth().getOffset(span, 1, Pointer.PointerType.FUTURE);

        assertEquals(construct(2006, 9, 16, 14), offsetSpan.getBeginCalendar());
        assertEquals(construct(2006, 9, 16, 14, 1), offsetSpan.getEndCalendar());

        offsetSpan = new RepeaterMonth().getOffset(span, 1, Pointer.PointerType.PAST);

        assertEquals(construct(2006, 7, 16, 14), offsetSpan.getBeginCalendar());
        assertEquals(construct(2006, 7, 16, 14, 1), offsetSpan.getEndCalendar());
    }
}

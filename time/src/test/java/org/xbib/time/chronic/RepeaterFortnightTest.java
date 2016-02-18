package org.xbib.time.chronic;

import org.junit.Assert;
import org.junit.Test;
import org.xbib.time.chronic.repeaters.RepeaterFortnight;
import org.xbib.time.chronic.repeaters.RepeaterWeek;
import org.xbib.time.chronic.tags.Pointer;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

public class RepeaterFortnightTest extends Assert {

    private ZonedDateTime now = construct(2006, 8, 16, 14, 0, 0);

    private final static ZoneId ZONE_ID = ZoneId.of("GMT");

    public static ZonedDateTime construct(int year, int month, int day) {
        return ZonedDateTime.of(year, month, day, 0, 0, 0, 0, ZONE_ID);
    }

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
    public void testNextFuture() {
        RepeaterFortnight fortnights = new RepeaterFortnight();
        fortnights.setNow(now);

        Span nextFortnight = fortnights.nextSpan(Pointer.PointerType.FUTURE);
        assertEquals(construct(2006, 8, 20), nextFortnight.getBeginCalendar());
        assertEquals(construct(2006, 9, 3), nextFortnight.getEndCalendar());

        Span nextNextFortnight = fortnights.nextSpan(Pointer.PointerType.FUTURE);
        assertEquals(construct(2006, 9, 3), nextNextFortnight.getBeginCalendar());
        assertEquals(construct(2006, 9, 17), nextNextFortnight.getEndCalendar());
    }

    @Test
    public void testNextPast() {
        RepeaterFortnight fortnights = new RepeaterFortnight();
        fortnights.setNow(now);
        Span lastFortnight = fortnights.nextSpan(Pointer.PointerType.PAST);
        assertEquals(construct(2006, 7, 30), lastFortnight.getBeginCalendar());
        assertEquals(construct(2006, 8, 13), lastFortnight.getEndCalendar());

        Span lastLastFortnight = fortnights.nextSpan(Pointer.PointerType.PAST);
        assertEquals(construct(2006, 7, 16), lastLastFortnight.getBeginCalendar());
        assertEquals(construct(2006, 7, 30), lastLastFortnight.getEndCalendar());
    }

    @Test
    public void testThisFuture() {
        RepeaterFortnight fortnights = new RepeaterFortnight();
        fortnights.setNow(now);

        Span thisFortnight = fortnights.thisSpan(Pointer.PointerType.FUTURE);
        assertEquals(construct(2006, 8, 16, 15), thisFortnight.getBeginCalendar());
        assertEquals(construct(2006, 8, 27), thisFortnight.getEndCalendar());
    }

    @Test
    public void testThisPast() {
        RepeaterFortnight fortnights = new RepeaterFortnight();
        fortnights.setNow(now);

        Span thisFortnight = fortnights.thisSpan(Pointer.PointerType.PAST);
        assertEquals(construct(2006, 8, 13, 0), thisFortnight.getBeginCalendar());
        assertEquals(construct(2006, 8, 16, 14), thisFortnight.getEndCalendar());
    }

    @Test
    public void testOffset() {
        Span span = new Span(now, ChronoUnit.SECONDS, 1);

        Span offsetSpan = new RepeaterWeek().getOffset(span, 3, Pointer.PointerType.FUTURE);

        assertEquals(construct(2006, 9, 6, 14), offsetSpan.getBeginCalendar());
        assertEquals(construct(2006, 9, 6, 14, 0, 1), offsetSpan.getEndCalendar());
    }
}

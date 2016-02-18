package org.xbib.time.chronic;

import org.junit.Assert;
import org.junit.Test;
import org.xbib.time.chronic.repeaters.RepeaterWeekend;
import org.xbib.time.chronic.tags.Pointer;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

public class RepeaterWeekendTest extends Assert {

    private ZonedDateTime now = construct(2006, 8, 16, 14, 0, 0);

    private final static ZoneId ZONE_ID = ZoneId.of("GMT");

    public static ZonedDateTime construct(int year, int month, int day) {
        return ZonedDateTime.of(year, month, day, 0, 0, 0, 0, ZONE_ID);
    }

    public static ZonedDateTime construct(int year, int month, int day, int hour, int minutes, int seconds) {
        return ZonedDateTime.of(year, month, day, hour, minutes, seconds, 0, ZONE_ID);
    }

    @Test
    public void testNextFuture() {
        RepeaterWeekend weekends = new RepeaterWeekend();
        weekends.setNow(now);

        Span nextWeekend = weekends.nextSpan(Pointer.PointerType.FUTURE);
        assertEquals(construct(2006, 8, 19), nextWeekend.getBeginCalendar());
        assertEquals(construct(2006, 8, 21), nextWeekend.getEndCalendar());
    }

    @Test
    public void testNextPast() {
        RepeaterWeekend weekends = new RepeaterWeekend();
        weekends.setNow(now);
        Span lastWeekend = weekends.nextSpan(Pointer.PointerType.PAST);
        assertEquals(construct(2006, 8, 12), lastWeekend.getBeginCalendar());
        assertEquals(construct(2006, 8, 14), lastWeekend.getEndCalendar());
    }

    @Test
    public void testThisFuture() {
        RepeaterWeekend weekends = new RepeaterWeekend();
        weekends.setNow(now);

        Span thisWeekend = weekends.thisSpan(Pointer.PointerType.FUTURE);
        assertEquals(construct(2006, 8, 19), thisWeekend.getBeginCalendar());
        assertEquals(construct(2006, 8, 21), thisWeekend.getEndCalendar());
    }

    @Test
    public void testThisPast() {
        RepeaterWeekend weekends = new RepeaterWeekend();
        weekends.setNow(now);

        Span thisWeekend = weekends.thisSpan(Pointer.PointerType.PAST);
        assertEquals(construct(2006, 8, 12), thisWeekend.getBeginCalendar());
        assertEquals(construct(2006, 8, 14), thisWeekend.getEndCalendar());
    }

    @Test
    public void testThisNone() {
        RepeaterWeekend weekends = new RepeaterWeekend();
        weekends.setNow(now);

        Span thisWeekend = weekends.thisSpan(Pointer.PointerType.FUTURE);
        assertEquals(construct(2006, 8, 19), thisWeekend.getBeginCalendar());
        assertEquals(construct(2006, 8, 21), thisWeekend.getEndCalendar());
    }

    @Test
    public void testOffset() {
        Span span = new Span(now, ChronoUnit.SECONDS, 1);

        Span offsetSpan;

        offsetSpan = new RepeaterWeekend().getOffset(span, 3, Pointer.PointerType.FUTURE);
        assertEquals(construct(2006, 9, 2), offsetSpan.getBeginCalendar());
        assertEquals(construct(2006, 9, 2, 0, 0, 1), offsetSpan.getEndCalendar());

        offsetSpan = new RepeaterWeekend().getOffset(span, 1, Pointer.PointerType.PAST);
        assertEquals(construct(2006, 8, 12), offsetSpan.getBeginCalendar());
        assertEquals(construct(2006, 8, 12, 0, 0, 1), offsetSpan.getEndCalendar());

        offsetSpan = new RepeaterWeekend().getOffset(span, 0, Pointer.PointerType.FUTURE);
        assertEquals(construct(2006, 8, 12), offsetSpan.getBeginCalendar());
        assertEquals(construct(2006, 8, 12, 0, 0, 1), offsetSpan.getEndCalendar());
    }
}

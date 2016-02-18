package org.xbib.time.chronic;

import org.junit.Assert;
import org.junit.Test;
import org.xbib.time.chronic.repeaters.RepeaterYear;
import org.xbib.time.chronic.tags.Pointer;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

public class RepeaterYearTest extends Assert {

    private ZonedDateTime now = construct(2006, 8, 16, 14, 0, 0);

    private final static ZoneId ZONE_ID = ZoneId.of("GMT");

    public static ZonedDateTime construct(int year, int month, int day) {
        return ZonedDateTime.of(year, month, day, 0, 0, 0, 0, ZONE_ID);
    }

    public static ZonedDateTime construct(int year, int month, int day, int hour, int minutes, int seconds) {
        return ZonedDateTime.of(year, month, day, hour, minutes, seconds, 0, ZONE_ID);
    }

    public static ZonedDateTime construct(int year, int month, int day, int hour) {
        return ZonedDateTime.of(year, month, day, hour, 0, 0, 0, ZONE_ID);
    }

    @Test
    public void testNextFuture() {
        RepeaterYear years = new RepeaterYear();
        years.setNow(now);

        Span nextYear = years.nextSpan(Pointer.PointerType.FUTURE);
        assertEquals(construct(2007, 1, 1), nextYear.getBeginCalendar());
        assertEquals(construct(2008, 1, 1), nextYear.getEndCalendar());

        Span nextNextYear = years.nextSpan(Pointer.PointerType.FUTURE);
        assertEquals(construct(2008, 1, 1), nextNextYear.getBeginCalendar());
        assertEquals(construct(2009, 1, 1), nextNextYear.getEndCalendar());
    }

    @Test
    public void testNextPast() {
        RepeaterYear years = new RepeaterYear();
        years.setNow(now);
        Span lastYear = years.nextSpan(Pointer.PointerType.PAST);
        assertEquals(construct(2005, 1, 1), lastYear.getBeginCalendar());
        assertEquals(construct(2006, 1, 1), lastYear.getEndCalendar());

        Span lastLastYear = years.nextSpan(Pointer.PointerType.PAST);
        assertEquals(construct(2004, 1, 1), lastLastYear.getBeginCalendar());
        assertEquals(construct(2005, 1, 1), lastLastYear.getEndCalendar());
    }

    @Test
    public void testThis() {
        RepeaterYear years = new RepeaterYear();
        years.setNow(now);

        Span thisYear;
        thisYear = years.thisSpan(Pointer.PointerType.FUTURE);
        assertEquals(construct(2006, 8, 17), thisYear.getBeginCalendar());
        assertEquals(construct(2007, 1, 1), thisYear.getEndCalendar());

        thisYear = years.thisSpan(Pointer.PointerType.PAST);
        assertEquals(construct(2006, 1, 1), thisYear.getBeginCalendar());
        assertEquals(construct(2006, 8, 16), thisYear.getEndCalendar());
    }

    @Test
    public void testOffset() {
        Span span = new Span(now, ChronoUnit.SECONDS, 1);

        Span offsetSpan;
        offsetSpan = new RepeaterYear().getOffset(span, 3, Pointer.PointerType.FUTURE);

        assertEquals(construct(2009, 8, 16, 14), offsetSpan.getBeginCalendar());
        assertEquals(construct(2009, 8, 16, 14, 0, 1), offsetSpan.getEndCalendar());

        offsetSpan = new RepeaterYear().getOffset(span, 10, Pointer.PointerType.PAST);

        assertEquals(construct(1996, 8, 16, 14), offsetSpan.getBeginCalendar());
        assertEquals(construct(1996, 8, 16, 14, 0, 1), offsetSpan.getEndCalendar());
    }
}

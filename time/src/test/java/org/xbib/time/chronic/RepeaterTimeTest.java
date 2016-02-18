package org.xbib.time.chronic;

import org.junit.Assert;
import org.junit.Test;
import org.xbib.time.chronic.repeaters.RepeaterTime;
import org.xbib.time.chronic.tags.Pointer;

import java.time.ZoneId;
import java.time.ZonedDateTime;

public class RepeaterTimeTest extends Assert {

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
    public void testNextFuture() {
        RepeaterTime t;

        t = new RepeaterTime("4:00");
        t.setNow(now);

        assertEquals(construct(2006, 8, 16, 16), t.nextSpan(Pointer.PointerType.FUTURE).getBeginCalendar());
        assertEquals(construct(2006, 8, 17, 4), t.nextSpan(Pointer.PointerType.FUTURE).getBeginCalendar());

        t = new RepeaterTime("13:00");
        t.setNow(now);

        assertEquals(construct(2006, 8, 17, 13), t.nextSpan(Pointer.PointerType.FUTURE).getBeginCalendar());
        assertEquals(construct(2006, 8, 18, 13), t.nextSpan(Pointer.PointerType.FUTURE).getBeginCalendar());

        t = new RepeaterTime("0400");
        t.setNow(now);

        assertEquals(construct(2006, 8, 17, 4), t.nextSpan(Pointer.PointerType.FUTURE).getBeginCalendar());
        assertEquals(construct(2006, 8, 18, 4), t.nextSpan(Pointer.PointerType.FUTURE).getBeginCalendar());
    }

    @Test
    public void testNextPast() {
        RepeaterTime t;
        t = new RepeaterTime("4:00");
        t.setNow(now);

        assertEquals(construct(2006, 8, 16, 4), t.nextSpan(Pointer.PointerType.PAST).getBeginCalendar());
        assertEquals(construct(2006, 8, 15, 16), t.nextSpan(Pointer.PointerType.PAST).getBeginCalendar());

        t = new RepeaterTime("13:00");
        t.setNow(now);

        assertEquals(construct(2006, 8, 16, 13), t.nextSpan(Pointer.PointerType.PAST).getBeginCalendar());
        assertEquals(construct(2006, 8, 15, 13), t.nextSpan(Pointer.PointerType.PAST).getBeginCalendar());
    }

    @Test
    public void testType() {
        RepeaterTime t1;
        t1 = new RepeaterTime("4");
        assertEquals(14400, t1.getType().intValue());

        t1 = new RepeaterTime("14");
        assertEquals(50400, t1.getType().intValue());

        t1 = new RepeaterTime("4:00");
        assertEquals(14400, t1.getType().intValue());

        t1 = new RepeaterTime("4:30");
        assertEquals(16200, t1.getType().intValue());

        t1 = new RepeaterTime("1400");
        assertEquals(50400, t1.getType().intValue());

        t1 = new RepeaterTime("0400");
        assertEquals(14400, t1.getType().intValue());

        t1 = new RepeaterTime("04");
        assertEquals(14400, t1.getType().intValue());

        t1 = new RepeaterTime("400");
        assertEquals(14400, t1.getType().intValue());
    }
}

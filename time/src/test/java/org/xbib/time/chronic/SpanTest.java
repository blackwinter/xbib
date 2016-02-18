package org.xbib.time.chronic;

import org.junit.Assert;
import org.junit.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;

public class SpanTest extends Assert {

    private final static ZoneId ZONE_ID = ZoneId.of("GMT");

    public static ZonedDateTime construct(int year, int month, int day, int hour) {
        return ZonedDateTime.of(year, month, day, hour, 0, 0, 0, ZONE_ID);
    }

    @Test
    public void testSpanWidth() {
        Span span = new Span(construct(2006, 8, 16, 0), construct(2006, 8, 17, 0));
        assertEquals(60L * 60L * 24L, (long)span.getWidth());
    }

    @Test
    public void testSpanMath() {
        Span span = new Span(1, 2, ZoneId.of("GMT"));
        assertEquals(2L, (long)span.add(1).getBegin());
        assertEquals(3L, (long)span.add(1).getEnd());
        assertEquals(0L, (long)span.add(-1).getBegin());
        assertEquals(1L, (long)span.add(-1).getEnd());
    }
}

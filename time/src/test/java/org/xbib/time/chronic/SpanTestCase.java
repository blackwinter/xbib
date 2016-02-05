package org.xbib.time.chronic;

import junit.framework.TestCase;

public class SpanTestCase extends TestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testSpanWidth() {
        Span span = new Span(Time.construct(2006, 8, 16, 0), Time.construct(2006, 8, 17, 0));
        assertEquals(60L * 60L * 24L, (long)span.getWidth());
    }

    public void testSpanMath() {
        Span span = new Span(1, 2);
        assertEquals(2L, (long)span.add(1).getBegin());
        assertEquals(3L, (long)span.add(1).getEnd());
        assertEquals(0L, (long)span.subtract(1).getBegin());
        assertEquals(1L, (long)span.subtract(1).getEnd());
    }
}

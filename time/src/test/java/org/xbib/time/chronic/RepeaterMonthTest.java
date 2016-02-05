package org.xbib.time.chronic;

import junit.framework.TestCase;
import org.xbib.time.chronic.repeaters.RepeaterMonth;
import org.xbib.time.chronic.tags.Pointer;

import java.util.Calendar;

public class RepeaterMonthTest extends TestCase {
    private Calendar _now;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        _now = Time.construct(2006, 8, 16, 14, 0, 0, 0);
    }

    public void testOffset() {
        Span span = new Span(_now, Calendar.SECOND, 60);

        Span offsetSpan;
        offsetSpan = new RepeaterMonth().getOffset(span, 1, Pointer.PointerType.FUTURE);

        assertEquals(Time.construct(2006, 9, 16, 14), offsetSpan.getBeginCalendar());
        assertEquals(Time.construct(2006, 9, 16, 14, 1), offsetSpan.getEndCalendar());

        offsetSpan = new RepeaterMonth().getOffset(span, 1, Pointer.PointerType.PAST);

        assertEquals(Time.construct(2006, 7, 16, 14), offsetSpan.getBeginCalendar());
        assertEquals(Time.construct(2006, 7, 16, 14, 1), offsetSpan.getEndCalendar());
    }
}

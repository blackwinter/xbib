package org.xbib.time.chronic;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

public class Span extends Range {

    public Span(Calendar begin, int field, long amount) {
        this(begin, Time.cloneAndAdd(begin, field, amount));
    }

    public Span(Calendar begin, Calendar end) {
        this(begin.getTimeInMillis() / 1000L, end.getTimeInMillis() / 1000L);
    }

    public Span(long begin, long end) {
        super(begin, end);
    }

    public Calendar getBeginCalendar() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(getBegin() * 1000);
        return cal;
    }

    public Calendar getEndCalendar() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(getEnd() * 1000);
        return cal;
    }

    /**
     * Add a number of seconds to this span, returning theresulting Span
     */
    public Span add(long seconds) {
        return new Span(getBegin() + seconds, getEnd() + seconds);
    }

    /**
     * Subtract a number of seconds to this span, returning the resulting Span
     */
    public Span subtract(long seconds) {
        return add(-seconds);
    }

    @Override
    public String toString() {
        Instant begin = getBeginCalendar().toInstant();
        Instant end = getEndCalendar().toInstant();
        return "(" + DateTimeFormatter.ISO_INSTANT.format(begin)
                + ".."
                + DateTimeFormatter.ISO_INSTANT.format(end)
                + ")";
    }
}

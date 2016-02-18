package org.xbib.time.chronic;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalUnit;

public class Span extends Range {

    private final ZoneId zoneId;

    public Span(ZonedDateTime begin, TemporalUnit field, int amount) {
        this(begin, begin.plus(amount, field));
    }

    public Span(ZonedDateTime begin, ZonedDateTime end) {
        this(begin.toInstant(), end.toInstant(), begin.getZone());
    }

    public Span(Instant begin, Instant end, ZoneId zoneId) {
        this(begin.getEpochSecond(), end.getEpochSecond(), zoneId);
    }

    public Span(long begin, long end, ZoneId zoneId) {
        super(begin, end);
        this.zoneId = zoneId;
    }

    public ZonedDateTime getBeginCalendar() {
        return ZonedDateTime.ofInstant(Instant.ofEpochSecond(getBegin()), zoneId);
    }

    public ZonedDateTime getEndCalendar() {
        return ZonedDateTime.ofInstant(Instant.ofEpochSecond(getEnd()), zoneId);
    }

    public Span add(long seconds) {
        return new Span(getBegin() + seconds, getEnd() + seconds, zoneId);
    }

    public ZoneId getZoneId() {
        return zoneId;
    }

    @Override
    public String toString() {
        return "(" + DateTimeFormatter.ISO_INSTANT.format(getBeginCalendar())
                + ".." + DateTimeFormatter.ISO_INSTANT.format( getEndCalendar()) + ")";
    }
}

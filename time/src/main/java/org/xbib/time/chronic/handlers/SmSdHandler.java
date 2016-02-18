package org.xbib.time.chronic.handlers;

import org.xbib.time.chronic.Options;
import org.xbib.time.chronic.Span;
import org.xbib.time.chronic.Token;
import org.xbib.time.chronic.tags.ScalarDay;
import org.xbib.time.chronic.tags.ScalarMonth;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class SmSdHandler implements IHandler {
    public Span handle(List<Token> tokens, Options options) {
        int month = tokens.get(0).getTag(ScalarMonth.class).getType();
        int day = tokens.get(1).getTag(ScalarDay.class).getType();
        ZonedDateTime start = ZonedDateTime.of(options.getNow().getYear(), month, day, 0, 0, 0, 0, options.getZoneId());
        ZonedDateTime end = start.plus(1, ChronoUnit.MONTHS);
        return new Span(start, end);
    }

}

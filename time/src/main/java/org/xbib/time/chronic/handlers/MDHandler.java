package org.xbib.time.chronic.handlers;

import org.xbib.time.chronic.Options;
import org.xbib.time.chronic.Span;
import org.xbib.time.chronic.Token;
import org.xbib.time.chronic.repeaters.Repeater;
import org.xbib.time.chronic.tags.Tag;

import java.time.ZonedDateTime;
import java.util.List;

public abstract class MDHandler implements IHandler {

    public Span handle(Repeater<?> monthRep, Tag<Integer> day, List<Token> timeTokens, Options options) {
        monthRep.setNow(options.getNow());
        Span span = monthRep.thisSpan(options.getContext());
        int year = span.getBeginCalendar().getYear();
        int month = span.getBeginCalendar().getMonthValue();
        ZonedDateTime dayStart = ZonedDateTime.of(year, month, day.getType(), 0, 0, 0, 0, options.getZoneId());
        return Handler.dayOrTime(dayStart, timeTokens, options);
    }
}

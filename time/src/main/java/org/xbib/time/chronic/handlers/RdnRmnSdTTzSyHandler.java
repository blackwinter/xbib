package org.xbib.time.chronic.handlers;

import org.xbib.time.chronic.Options;
import org.xbib.time.chronic.Span;
import org.xbib.time.chronic.Token;
import org.xbib.time.chronic.repeaters.RepeaterMonthName;
import org.xbib.time.chronic.tags.ScalarDay;
import org.xbib.time.chronic.tags.ScalarYear;

import java.time.ZonedDateTime;
import java.util.List;

public class RdnRmnSdTTzSyHandler implements IHandler {

    public Span handle(List<Token> tokens, Options options) {
        int month = tokens.get(1).getTag(RepeaterMonthName.class).getType().ordinal();
        int day = tokens.get(2).getTag(ScalarDay.class).getType();
        int year = tokens.get(5).getTag(ScalarYear.class).getType();
        Span span;
        try {
            List<Token> timeTokens = tokens.subList(3, 4);
            ZonedDateTime dayStart = ZonedDateTime.of(year, month, day, 0, 0, 0, 0, options.getZoneId());
            span = Handler.dayOrTime(dayStart, timeTokens, options);
        } catch (IllegalArgumentException e) {
            span = null;
        }
        return span;
    }
}

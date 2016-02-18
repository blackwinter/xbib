package org.xbib.time.chronic.handlers;

import org.xbib.time.chronic.Options;
import org.xbib.time.chronic.Span;
import org.xbib.time.chronic.Token;
import org.xbib.time.chronic.repeaters.RepeaterMonthName;
import org.xbib.time.chronic.tags.ScalarDay;
import org.xbib.time.chronic.tags.ScalarYear;

import java.time.ZonedDateTime;
import java.util.List;

public class RmnSdSyHandler implements IHandler {

    public Span handle(List<Token> tokens, Options options) {
        int month = tokens.get(0).getTag(RepeaterMonthName.class).getType().ordinal();
        int day = tokens.get(1).getTag(ScalarDay.class).getType();
        int year = tokens.get(2).getTag(ScalarYear.class).getType();
        Span span;
        try {
            List<Token> timeTokens = tokens.subList(3, tokens.size());
            ZonedDateTime dayStart = ZonedDateTime.of(year, month, day, 0, 0, 0, 0, options.getZoneId());
            span = Handler.dayOrTime(dayStart, timeTokens, options);
        } catch (IllegalArgumentException e) {
            span = null;
        }
        return span;
    }

}

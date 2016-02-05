package org.xbib.time.chronic.handlers;

import org.xbib.time.chronic.Options;
import org.xbib.time.chronic.Span;
import org.xbib.time.chronic.Time;
import org.xbib.time.chronic.Token;
import org.xbib.time.chronic.repeaters.RepeaterMonthName;
import org.xbib.time.chronic.tags.ScalarYear;

import java.util.Calendar;
import java.util.List;

public class RmnSyHandler implements IHandler {

    public Span handle(List<Token> tokens, Options options) {
        int month = tokens.get(0).getTag(RepeaterMonthName.class).getType().ordinal();
        int year = tokens.get(1).getTag(ScalarYear.class).getType();
        Span span;
        try {
            Calendar start = Time.construct(year, month);
            Calendar end = Time.cloneAndAdd(start, Calendar.MONTH, 1);
            span = new Span(start, end);
        } catch (IllegalArgumentException e) {
            span = null;
        }
        return span;
    }

}

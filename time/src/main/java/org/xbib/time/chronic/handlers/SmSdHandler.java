package org.xbib.time.chronic.handlers;

import org.xbib.time.chronic.Options;
import org.xbib.time.chronic.Span;
import org.xbib.time.chronic.Time;
import org.xbib.time.chronic.Token;
import org.xbib.time.chronic.tags.ScalarDay;
import org.xbib.time.chronic.tags.ScalarMonth;

import java.util.Calendar;
import java.util.List;

public class SmSdHandler implements IHandler {
    public Span handle(List<Token> tokens, Options options) {
        int month = tokens.get(0).getTag(ScalarMonth.class).getType();
        int day = tokens.get(1).getTag(ScalarDay.class).getType();
        Calendar start = Time.construct(options.getNow().get(Calendar.YEAR), month, day);
        Calendar end = Time.cloneAndAdd(start, Calendar.DAY_OF_MONTH, 1);
        return new Span(start, end);
    }

}

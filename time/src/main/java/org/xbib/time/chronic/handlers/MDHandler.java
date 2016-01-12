package org.xbib.time.chronic.handlers;

import org.xbib.time.chronic.Options;
import org.xbib.time.chronic.Span;
import org.xbib.time.chronic.Time;
import org.xbib.time.chronic.Token;
import org.xbib.time.chronic.repeaters.Repeater;
import org.xbib.time.chronic.tags.Tag;

import java.util.Calendar;
import java.util.List;

public abstract class MDHandler implements IHandler {

    public Span handle(Repeater<?> month, Tag<Integer> day, List<Token> timeTokens, Options options) {
        month.setStart((Calendar) options.getNow().clone());
        Span span = month.thisSpan(options.getContext());
        Calendar dayStart = Time.construct(span.getBeginCalendar().get(Calendar.YEAR), span.getBeginCalendar().get(Calendar.MONTH) + 1, day.getType());
        return Handler.dayOrTime(dayStart, timeTokens, options);
    }
}

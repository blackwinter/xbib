package org.xbib.time.chronic.handlers;

import org.xbib.time.chronic.Options;
import org.xbib.time.chronic.Span;
import org.xbib.time.chronic.Time;
import org.xbib.time.chronic.Token;
import org.xbib.time.chronic.repeaters.Repeater;
import org.xbib.time.chronic.tags.Ordinal;
import org.xbib.time.chronic.tags.Pointer;

import java.util.Calendar;
import java.util.List;

public abstract class ORRHandler implements IHandler {

    public Span handle(List<Token> tokens, Span outerSpan, Options options) {
        Repeater<?> repeater = tokens.get(1).getTag(Repeater.class);
        repeater.setStart(Time.cloneAndAdd(outerSpan.getBeginCalendar(), Calendar.SECOND, -1));
        Integer ordinalValue = tokens.get(0).getTag(Ordinal.class).getType();
        Span span = null;
        for (int i = 0; i < ordinalValue; i++) {
            span = repeater.nextSpan(Pointer.PointerType.FUTURE);
            if (span.getBegin() > outerSpan.getEnd()) {
                span = null;
                break;
            }
        }
        return span;
    }
}

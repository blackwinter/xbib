package org.xbib.time.chronic.handlers;

import org.xbib.time.chronic.Options;
import org.xbib.time.chronic.Span;
import org.xbib.time.chronic.Token;
import org.xbib.time.chronic.repeaters.RepeaterMonthName;
import org.xbib.time.chronic.tags.OrdinalDay;

import java.util.List;

public class RmnOdHandler extends MDHandler {
    public Span handle(List<Token> tokens, Options options) {
        return handle(tokens.get(0).getTag(RepeaterMonthName.class), tokens.get(1).getTag(OrdinalDay.class), tokens.subList(2, tokens.size()), options);
    }
}

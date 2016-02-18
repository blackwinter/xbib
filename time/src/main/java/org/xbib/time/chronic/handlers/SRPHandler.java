package org.xbib.time.chronic.handlers;

import org.xbib.time.chronic.Chronic;
import org.xbib.time.chronic.Options;
import org.xbib.time.chronic.Span;
import org.xbib.time.chronic.Token;
import org.xbib.time.chronic.repeaters.Repeater;
import org.xbib.time.chronic.tags.Pointer;
import org.xbib.time.chronic.tags.Scalar;

import java.text.ParseException;
import java.util.List;

public class SRPHandler implements IHandler {

    public Span handle(List<Token> tokens, Span span, Options options) {
        int distance = tokens.get(0).getTag(Scalar.class).getType();
        Repeater<?> repeater = tokens.get(1).getTag(Repeater.class);
        Pointer.PointerType pointer = tokens.get(2).getTag(Pointer.class).getType();
        return repeater.getOffset(span, distance, pointer);
    }

    @Override
    public Span handle(List<Token> tokens, Options options) {
        try {
            Span span = Chronic.parse("this second", new Options().setNow(options.getNow()).setGuess(false));
            return handle(tokens, span, options);
        } catch (ParseException e) {
            return null;
        }
    }
}

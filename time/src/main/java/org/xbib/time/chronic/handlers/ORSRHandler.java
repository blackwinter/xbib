package org.xbib.time.chronic.handlers;

import org.xbib.time.chronic.Options;
import org.xbib.time.chronic.Span;
import org.xbib.time.chronic.Token;

import java.util.List;

public class ORSRHandler extends ORRHandler {

    public Span handle(List<Token> tokens, Options options) {
        Span outerSpan = Handler.getAnchor(tokens.subList(3, 4), options);
        return handle(tokens.subList(0, 2), outerSpan, options);
    }

}

package org.xbib.time.chronic.handlers;

import org.xbib.time.chronic.Options;
import org.xbib.time.chronic.Span;
import org.xbib.time.chronic.Token;

import java.util.List;

public class SRPAHandler extends SRPHandler {

    @Override
    public Span handle(List<Token> tokens, Options options) {
        Span anchorSpan = Handler.getAnchor(tokens.subList(3, tokens.size()), options);
        return super.handle(tokens, anchorSpan, options);
    }

}

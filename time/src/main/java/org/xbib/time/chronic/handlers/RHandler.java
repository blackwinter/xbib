package org.xbib.time.chronic.handlers;

import org.xbib.time.chronic.Options;
import org.xbib.time.chronic.Span;
import org.xbib.time.chronic.Token;

import java.util.List;

public class RHandler implements IHandler {

    public Span handle(List<Token> tokens, Options options) {
        List<Token> ddTokens = Handler.dealiasAndDisambiguateTimes(tokens, options);
        return Handler.getAnchor(ddTokens, options);
    }

}

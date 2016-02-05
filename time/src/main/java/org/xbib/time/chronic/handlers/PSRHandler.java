package org.xbib.time.chronic.handlers;

import org.xbib.time.chronic.Options;
import org.xbib.time.chronic.Span;
import org.xbib.time.chronic.Token;

import java.util.LinkedList;
import java.util.List;

public class PSRHandler extends SRPHandler {

    @Override
    public Span handle(List<Token> tokens, Options options) {
        List<Token> newTokens = new LinkedList<>();
        newTokens.add(tokens.get(1));
        newTokens.add(tokens.get(2));
        newTokens.add(tokens.get(0));
        return super.handle(newTokens, options);
    }

}

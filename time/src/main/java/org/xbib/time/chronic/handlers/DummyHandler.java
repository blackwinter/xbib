package org.xbib.time.chronic.handlers;

import org.xbib.time.chronic.Options;
import org.xbib.time.chronic.Span;
import org.xbib.time.chronic.Token;

import java.util.List;

public class DummyHandler implements IHandler {
    public Span handle(List<Token> tokens, Options options) {
        return null;
    }
}

package org.xbib.jacc.compiler;

public class Failure extends Diagnostic {

    public Failure(String s) {
        super(s);
    }

    public Failure(Position position, String s) {
        super(position, s);
    }
}

package org.xbib.jacc.compiler;

public class Warning extends Diagnostic {

    public Warning(String s) {
        super(s);
    }

    public Warning(Position position, String s) {
        super(position, s);
    }
}

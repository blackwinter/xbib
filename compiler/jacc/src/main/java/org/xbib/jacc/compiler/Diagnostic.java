package org.xbib.jacc.compiler;

public abstract class Diagnostic extends Exception {

    private String text;
    private Position position;

    String getText() {
        return text;
    }

    Position getPos() {
        return position;
    }

    Diagnostic(String s) {
        text = s;
    }

    Diagnostic(Position position, String s) {
        this.position = position;
        text = s;
    }
}

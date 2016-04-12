package org.xbib.io.field;

public abstract class AbstractSeparable implements Separable {

    private final char sep;

    private final String content;

    AbstractSeparable(char sep, String content) {
        this.sep = sep;
        this.content = content;
    }

    public char getFieldSeparator() {
        return sep;
    }

    public String getField() {
        return content;
    }
}

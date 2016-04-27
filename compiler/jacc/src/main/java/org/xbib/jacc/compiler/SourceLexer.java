package org.xbib.jacc.compiler;

import java.io.IOException;

public abstract class SourceLexer extends Lexer {

    protected String line;
    protected int col;
    protected int c;
    private Source source;
    private SourcePosition pos;

    public SourceLexer(Handler handler, Source source1) throws IOException {
        super(handler);
        col = -1;
        source = source1;
        pos = new SourcePosition(source1);
        line = source1.readLine();
        nextChar();
    }

    public Position getPos() {
        return pos.copy();
    }

    protected void markPosition() {
        pos.updateCoords(source.getLineNo(), col);
    }

    protected void nextLine() throws IOException {
        line = source.readLine();
        col = -1;
        nextChar();
    }

    protected int nextChar() {
        if (line == null) {
            c = -1;
            col = 0;
        } else {
            if (++col >= line.length()) {
                c = 10;
            } else {
                c = line.charAt(col);
            }
        }
        return c;
    }

    public void close() throws IOException {
        if (source != null) {
            source.close();
            source = null;
        }
    }
}

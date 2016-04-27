package org.xbib.jacc.compiler;

import java.io.IOException;

public abstract class Source extends Phase {

    Source(Handler handler)
    {
        super(handler);
    }

    public abstract String describe();

    public abstract String readLine() throws IOException;

    public abstract int getLineNo();

    String getLine(int i)
    {
        return null;
    }

    public void close() throws IOException {
    }
}

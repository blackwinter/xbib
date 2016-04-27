package org.xbib.jacc.compiler;

import java.io.IOException;

public abstract class Lexer extends Phase {

    protected int token;

    protected String lexemeText;

    Lexer(Handler handler)
    {
        super(handler);
    }

    public abstract int nextToken() throws IOException;

    public int getToken()
    {
        return token;
    }

    public String getLexeme()
    {
        return lexemeText;
    }

    public abstract Position getPos();

    public boolean match(int i) throws IOException {
        if (i == token)
        {
            nextToken();
            return true;
        } else
        {
            return false;
        }
    }

    public abstract void close() throws IOException;
}

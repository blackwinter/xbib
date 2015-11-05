// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) fieldsfirst space 

package compiler;


// Referenced classes of package compiler:
//            Phase, Handler, Position

public abstract class Lexer extends Phase
{

    protected int token;
    protected String lexemeText;

    public Lexer(Handler handler)
    {
        super(handler);
    }

    public Lexer(Handler handler, int i)
    {
        super(handler);
        token = i;
    }

    public abstract int nextToken();

    public int getToken()
    {
        return token;
    }

    public String getLexeme()
    {
        return lexemeText;
    }

    public abstract Position getPos();

    public boolean match(int i)
    {
        if (i == token)
        {
            nextToken();
            return true;
        } else
        {
            return false;
        }
    }

    public abstract void close();
}

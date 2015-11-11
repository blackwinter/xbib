package compiler;

public abstract class SourceLexer extends Lexer
{

    protected Source source;
    protected String line;
    protected int col;
    protected SourcePosition pos;
    protected static final int EOF = -1;
    protected static final int EOL = 10;
    protected int c;

    public SourceLexer(Handler handler, Source source1)
    {
        super(handler);
        col = -1;
        source = source1;
        pos = new SourcePosition(source1);
        line = source1.readLine();
        nextChar();
    }

    public Position getPos()
    {
        return pos.copy();
    }

    protected void markPosition()
    {
        pos.updateCoords(source.getLineNo(), col);
    }

    protected void nextLine()
    {
        line = source.readLine();
        col = -1;
        nextChar();
    }

    protected int nextChar()
    {
        if (line == null)
        {
            c = -1;
            col = 0;
        } else
        if (++col >= line.length())
            c = 10;
        else
            c = line.charAt(col);
        return c;
    }

    protected int nextChar(int i)
    {
        if (line == null)
            c = -1;
        else
        if ((col += i) >= line.length())
            c = 10;
        else
            c = line.charAt(col);
        return c;
    }

    public void close()
    {
        if (source != null)
        {
            source.close();
            source = null;
        }
    }
}

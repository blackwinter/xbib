package compiler;

public abstract class Diagnostic extends Exception
{

    private String text;
    private Position position;
    protected String crossRef;

    public String getText()
    {
        return text;
    }

    public Position getPos()
    {
        return position;
    }

    public String getCrossRef()
    {
        return null;
    }

    public Diagnostic(String s)
    {
        text = s;
    }

    public Diagnostic(Position position1)
    {
        position = position1;
    }

    public Diagnostic(Position position1, String s)
    {
        position = position1;
        text = s;
    }
}

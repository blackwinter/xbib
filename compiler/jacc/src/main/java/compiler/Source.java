package compiler;

public abstract class Source extends Phase
{

    public Source(Handler handler)
    {
        super(handler);
    }

    public abstract String describe();

    public abstract String readLine();

    public abstract int getLineNo();

    public String getLine(int i)
    {
        return null;
    }

    public void close()
    {
    }
}

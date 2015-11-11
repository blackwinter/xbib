package compiler;

public class StringArraySource extends Source
{

    private String text;
    private String lines[];
    private int count;

    public StringArraySource(Handler handler, String s, String as[])
    {
        super(handler);
        count = 0;
        text = s;
        lines = as;
        count = 0;
    }

    public String describe()
    {
        return text;
    }

    public String readLine()
    {
        if (lines == null || count >= lines.length)
            return null;
        else
            return lines[count++];
    }

    public int getLineNo()
    {
        return count;
    }

    public String getLine(int i)
    {
        if (lines == null || i < 1 || i > lines.length)
            return null;
        else
            return lines[count - 1];
    }

    public void close()
    {
        lines = null;
    }
}

package compiler;

public class HistorySource extends Source
{

    private Source source;
    private String history[];

    public HistorySource(Handler handler, int i, Source source1)
    {
        super(handler);
        source = source1;
        history = i <= 0 ? null : new String[i];
    }

    public String describe()
    {
        return source.describe();
    }

    public String readLine()
    {
        String s = source.readLine();
        if (history != null)
        {
            int i = source.getLineNo() % history.length;
            history[i] = s;
        }
        return s;
    }

    public int getLineNo()
    {
        return source.getLineNo();
    }

    public String getLine(int i)
    {
        if (history != null && i > 0)
        {
            int j = source.getLineNo();
            if (i > j - history.length && i <= j)
                return history[i % history.length];
        }
        return source.getLine(i);
    }

    public void close()
    {
        source.close();
        history = null;
    }
}

package compiler;

public class LiterateSource extends Source
{

    private Source source;
    private boolean strict;
    private static final int BLANK = 0;
    private static final int COMMENT = 1;
    private static final int CODE = 2;
    private int lines;
    private int last;
    private char tag;

    public LiterateSource(Handler handler, boolean flag, Source source1)
    {
        super(handler);
        lines = 0;
        last = 0;
        tag = '>';
        source = source1;
        strict = flag;
    }

    public LiterateSource(Handler handler, Source source1)
    {
        this(handler, true, source1);
    }

    public String describe()
    {
        return source.describe();
    }

    public String readLine()
    {
        String s = source.readLine();
        if (s == null)
        {
            return null;
        }
        if (s.length() > 0 && s.charAt(0) == tag)
        {
            last = 2;
            lines++;
            return " " + s.substring(1);
        }
        for (int i = s.length(); 0 < i--;)
            if (!Character.isWhitespace(s.charAt(i)))
            {
                last = 1;
                return "";
            }

        last = 0;
        return "";
    }

    public int getLineNo()
    {
        return source.getLineNo();
    }

    public String getLine(int i)
    {
        return source.getLine(i);
    }

    public void close()
    {
        source.close();
    }
}

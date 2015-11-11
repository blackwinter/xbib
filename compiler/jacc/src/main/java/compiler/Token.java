package compiler;

public class Token
{

    private int code;
    private String text;

    public Token(int i, String s)
    {
        code = i;
        text = s;
    }

    public Token(int i)
    {
        this(i, null);
    }

    public int getCode()
    {
        return code;
    }

    public String getText()
    {
        return text;
    }
}

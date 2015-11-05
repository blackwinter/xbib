package compiler;

public class TokenArrayLexer extends Lexer
{

    private Token tokens[];
    private int count;

    public TokenArrayLexer(Handler handler, Token atoken[])
    {
        super(handler);
        count = 0;
        tokens = atoken;
    }

    public int nextToken()
    {
        if (tokens == null || count >= tokens.length)
        {
            lexemeText = null;
            return token = 0;
        } else
        {
            token = tokens[count].getCode();
            lexemeText = tokens[count].getText();
            count++;
            return token;
        }
    }

    public Position getPos()
    {
        return new TokenArrayPosition(count);
    }

    public void close()
    {
        tokens = null;
    }
}

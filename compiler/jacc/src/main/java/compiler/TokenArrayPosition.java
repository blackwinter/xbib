package compiler;

public class TokenArrayPosition extends Position
{

    private int pos;

    public TokenArrayPosition(int i)
    {
        pos = i;
    }

    public String describe()
    {
        return "Token #" + pos;
    }

    public Position copy()
    {
        return new TokenArrayPosition(pos);
    }
}

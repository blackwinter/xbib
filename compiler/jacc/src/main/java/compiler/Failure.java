package compiler;

public class Failure extends Diagnostic
{

    public Failure(String s)
    {
        super(s);
    }

    public Failure(Position position)
    {
        super(position);
    }

    public Failure(Position position, String s)
    {
        super(position, s);
    }
}

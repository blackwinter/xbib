package compiler;


public abstract class Position
{

    public Position()
    {
    }

    public abstract String describe();

    public int getColumn()
    {
        return 0;
    }

    public int getRow()
    {
        return 0;
    }

    public abstract Position copy();
}

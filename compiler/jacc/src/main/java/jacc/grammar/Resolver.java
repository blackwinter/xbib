package jacc.grammar;

public abstract class Resolver
{

    public Resolver()
    {
    }

    public abstract void srResolve(Tables tables, int i, int j, int k);

    public abstract void rrResolve(Tables tables, int i, int j, int k);
}

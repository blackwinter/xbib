package jacc.util;

public class ElemInterator extends Interator
{

    private int count;
    private int limit;
    private int a[];

    public ElemInterator(int ai[], int i, int j)
    {
        a = ai;
        count = i;
        limit = j;
    }

    public ElemInterator(int ai[])
    {
        this(ai, 0, ai.length);
    }

    public int next()
    {
        return a[count++];
    }

    public boolean hasNext()
    {
        return count < limit;
    }
}

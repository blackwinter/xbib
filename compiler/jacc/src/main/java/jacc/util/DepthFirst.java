package jacc.util;

public abstract class DepthFirst
{

    private Interator seq;
    protected int adjs[][];
    private int visited[];

    DepthFirst(Interator interator, int ai[][])
    {
        seq = interator;
        adjs = ai;
        visited = BitSet.make(ai.length);
    }

    protected void search()
    {
        do
        {
            if (!seq.hasNext())
                break;
            if (visit(seq.next()))
                doneTree();
        } while (true);
    }

    private boolean visit(int i)
    {
        if (BitSet.addTo(visited, i))
        {
            int ai[] = adjs[i];
            for (int j = 0; j < ai.length; j++)
                visit(ai[j]);

            doneVisit(i);
            return true;
        } else
        {
            return false;
        }
    }

    void doneVisit(int i)
    {
    }

    void doneTree()
    {
    }
}

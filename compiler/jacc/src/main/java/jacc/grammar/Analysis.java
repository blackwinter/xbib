package jacc.grammar;


public abstract class Analysis
{

    private int comps[][];

    protected Analysis(int ai[][])
    {
        comps = ai;
    }

    protected void bottomUp()
    {
        for (int i = 0; i < comps.length; i++)
            analyzeComponent(comps[i]);

    }

    protected void topDown()
    {
        for (int i = comps.length; i-- > 0;)
            analyzeComponent(comps[i]);

    }

    private void analyzeComponent(int ai[])
    {
        for (boolean flag = true; flag;)
        {
            flag = false;
            int i = 0;
            while (i < ai.length) 
            {
                flag |= analyze(ai[i]);
                i++;
            }
        }

    }

    protected abstract boolean analyze(int i);
}

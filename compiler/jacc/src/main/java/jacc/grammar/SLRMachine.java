package jacc.grammar;

import jacc.util.IntSet;
import java.io.PrintWriter;

public class SLRMachine extends LookaheadMachine
{

    private Follow follow;
    private int laReds[][][];

    public SLRMachine(Grammar grammar)
    {
        super(grammar);
        follow = grammar.getFollow();
        calcLookahead();
    }

    public int[] getLookaheadAt(int i, int j)
    {
        return laReds[i][j];
    }

    private void calcLookahead()
    {
        laReds = new int[numStates][][];
        for (int i = 0; i < numStates; i++)
        {
            IntSet intset = getItemsAt(i);
            int ai[] = getReducesAt(i);
            laReds[i] = new int[ai.length][];
            for (int j = 0; j < ai.length; j++)
            {
                int k = items.getItem(intset.at(ai[j])).getLhs();
                laReds[i][j] = follow.at(k);
            }

        }

    }

    public void display(PrintWriter printwriter)
    {
        super.display(printwriter);
        for (int i = 0; i < numStates; i++)
        {
            IntSet intset = getItemsAt(i);
            int ai[] = getReducesAt(i);
            if (ai.length <= 0)
                continue;
            printwriter.println("In state " + i + ":");
            for (int j = 0; j < ai.length; j++)
            {
                printwriter.print(" Item: ");
                items.getItem(intset.at(ai[j])).display(printwriter);
                printwriter.println();
                printwriter.print("  Lookahead: {");
                printwriter.print(grammar.displaySymbolSet(laReds[i][j], numNTs));
                printwriter.println("}");
            }

        }

    }
}

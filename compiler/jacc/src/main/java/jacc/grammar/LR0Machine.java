package jacc.grammar;

import jacc.util.BitSet;
import java.io.PrintWriter;

public class LR0Machine extends LookaheadMachine
{

    int allTokens[];

    public LR0Machine(Grammar grammar)
    {
        super(grammar);
        int i = grammar.getNumTs();
        allTokens = BitSet.make(i);
        for (int j = 0; j < i; j++)
            BitSet.set(allTokens, j);

    }

    public int[] getLookaheadAt(int i, int j)
    {
        return allTokens;
    }

    public void display(PrintWriter printwriter)
    {
        super.display(printwriter);
        printwriter.print("Lookahead set is {");
        printwriter.print(grammar.displaySymbolSet(allTokens, numNTs));
        printwriter.println("}");
    }
}

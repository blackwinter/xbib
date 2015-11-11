package jacc.grammar;

import jacc.util.BitSet;

import java.io.PrintWriter;

public final class Left extends Analysis {

    private Grammar grammar;
    private int numNTs;
    private int left[][];

    public Left(Grammar grammar1) {
        super(grammar1.getComponents());
        grammar = grammar1;
        numNTs = grammar1.getNumNTs();
        left = new int[numNTs][];
        for (int i = 0; i < numNTs; i++) {
            left[i] = BitSet.make(numNTs);
            BitSet.set(left[i], i);
        }

        bottomUp();
    }

    protected boolean analyze(int i) {
        boolean flag = false;
        Grammar.Prod aprod[] = grammar.getProds(i);
        for (int j = 0; j < aprod.length; j++) {
            int ai[] = aprod[j].getRhs();
            if (ai.length > 0 && grammar.isNonterminal(ai[0]) && BitSet.addTo(left[i], left[ai[0]])) {
                flag = true;
            }
        }

        return flag;
    }

    public int[] at(int i) {
        return left[i];
    }

    public void display(PrintWriter printwriter) {
        printwriter.println("Left nonterminal sets:");
        for (int i = 0; i < left.length; i++) {
            printwriter.print(" Left(" + grammar.getSymbol(i) + "): {");
            printwriter.print(grammar.displaySymbolSet(left[i], 0));
            printwriter.println("}");
        }

    }
}

package org.xbib.jacc.grammar;

import org.xbib.jacc.util.BitSet;

import java.io.IOException;
import java.io.Writer;

public final class Left extends Analysis {

    private Grammar grammar;
    private int[][] left;

    Left(Grammar grammar1) {
        super(grammar1.getComponents());
        grammar = grammar1;
        int numNTs = grammar1.getNumNTs();
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
        for (Grammar.Prod anAprod : aprod) {
            int ai[] = anAprod.getRhs();
            if (ai.length > 0 && grammar.isNonterminal(ai[0]) && BitSet.addTo(left[i], left[ai[0]])) {
                flag = true;
            }
        }
        return flag;
    }

    public int[] at(int i) {
        return left[i];
    }

    public void display(Writer writer) throws IOException {
        writer.write("Left nonterminal sets:\n");
        for (int i = 0; i < left.length; i++) {
            writer.write(" Left(" + grammar.getSymbol(i) + "): {");
            writer.write(grammar.displaySymbolSet(left[i], 0));
            writer.write("}\n");
        }
    }
}

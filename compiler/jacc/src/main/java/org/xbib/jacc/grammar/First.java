package org.xbib.jacc.grammar;

import org.xbib.jacc.util.BitSet;

import java.io.IOException;
import java.io.Writer;

public class First extends Analysis {

    private Grammar grammar;
    private Nullable nullable;
    private int numNTs;
    private int[][] first;

    First(Grammar grammar1, Nullable nullable1) {
        super(grammar1.getComponents());
        grammar = grammar1;
        nullable = nullable1;
        numNTs = grammar1.getNumNTs();
        int numTs = grammar1.getNumTs();
        first = new int[numNTs][];
        for (int i = 0; i < numNTs; i++) {
            first[i] = BitSet.make(numTs);
        }

        bottomUp();
    }

    protected boolean analyze(int i) {
        boolean flag = false;
        Grammar.Prod aprod[] = grammar.getProds(i);
label0:
        for (Grammar.Prod anAprod : aprod) {
            int ai[] = anAprod.getRhs();
            int k = 0;
            do {
                if (k >= ai.length) {
                    continue label0;
                }
                if (grammar.isTerminal(ai[k])) {
                    if (BitSet.addTo(first[i], ai[k] - numNTs)) {
                        flag = true;
                    }
                    continue label0;
                }
                if (BitSet.addTo(first[i], first[ai[k]])) {
                    flag = true;
                }
                if (!nullable.at(ai[k])) {
                    continue label0;
                }
                k++;
            } while (true);
        }

        return flag;
    }

    public int[] at(int i) {
        return first[i];
    }

    public void display(Writer writer) throws IOException {
        writer.write("First sets:\n");
        for (int i = 0; i < first.length; i++) {
            writer.write(" First(" + grammar.getSymbol(i) + "): {");
            writer.write(grammar.displaySymbolSet(at(i), numNTs));
            writer.write("}\n");
        }
    }
}

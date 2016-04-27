package org.xbib.jacc.grammar;

import org.xbib.jacc.util.BitSet;

import java.io.IOException;
import java.io.Writer;

public class LR0Machine extends LookaheadMachine {

    private int allTokens[];

    public LR0Machine(Grammar grammar) {
        super(grammar);
        int i = grammar.getNumTs();
        allTokens = BitSet.make(i);
        for (int j = 0; j < i; j++) {
            BitSet.set(allTokens, j);
        }
    }

    public int[] getLookaheadAt(int i, int j)
    {
        return allTokens;
    }

    public void display(Writer writer) throws IOException {
        super.display(writer);
        writer.write("Lookahead set is {");
        writer.write(grammar.displaySymbolSet(allTokens, numNTs));
        writer.write("}\n");
    }
}

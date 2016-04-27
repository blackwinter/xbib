package org.xbib.jacc.grammar;

import org.xbib.jacc.util.IntSet;

import java.io.IOException;
import java.io.Writer;

public class SLRMachine extends LookaheadMachine {

    private Follow follow;
    private int[][][] laReds;

    public SLRMachine(Grammar grammar) {
        super(grammar);
        follow = grammar.getFollow();
        calcLookahead();
    }

    public int[] getLookaheadAt(int i, int j) {
        return laReds[i][j];
    }

    private void calcLookahead() {
        laReds = new int[numStates][][];
        for (int i = 0; i < numStates; i++) {
            IntSet intset = getItemsAt(i);
            int[] ai = getReducesAt(i);
            laReds[i] = new int[ai.length][];
            for (int j = 0; j < ai.length; j++) {
                int k = items.getItem(intset.at(ai[j])).getLhs();
                laReds[i][j] = follow.at(k);
            }
        }
    }

    public void display(Writer writer) throws IOException {
        super.display(writer);
        for (int i = 0; i < numStates; i++) {
            IntSet intset = getItemsAt(i);
            int[] ai = getReducesAt(i);
            if (ai.length <= 0) {
                continue;
            }
            writer.write("In state " + i + ":");
            for (int j = 0; j < ai.length; j++) {
                writer.write(" Item: ");
                items.getItem(intset.at(ai[j])).display(writer);
                writer.write("\n");
                writer.write("  Lookahead: {");
                writer.write(grammar.displaySymbolSet(laReds[i][j], numNTs));
                writer.write("}\n");
            }
        }
    }
}

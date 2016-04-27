package org.xbib.jacc.grammar;

import java.io.IOException;
import java.io.Writer;

public final class Finitary extends Analysis {

    private boolean finitary[];
    private boolean consider[];
    private Grammar grammar;
    private int numNTs;

    Finitary(Grammar grammar) {
        super(grammar.getComponents());
        this.grammar = grammar;
        numNTs = grammar.getNumNTs();
        finitary = new boolean[numNTs];
        consider = new boolean[numNTs];
        for (int i = 0; i < numNTs; i++) {
            finitary[i] = false;
            consider[i] = true;
        }
        bottomUp();
    }

    protected boolean analyze(int i) {
        boolean flag = false;
        if (consider[i]) {
            int j = 0;
            Grammar.Prod aprod[] = grammar.getProds(i);
            for (Grammar.Prod anAprod : aprod) {
                int ai[] = anAprod.getRhs();
                int l;
                l = 0;
                while (l < ai.length && at(ai[l])) {
                    l++;
                }
                if (l >= ai.length) {
                    finitary[i] = true;
                    consider[i] = false;
                    flag = true;
                    break;
                }
                if (!consider[ai[l]]) {
                    j++;
                }
            }
            if (j == aprod.length) {
                consider[i] = false;
            }
        }
        return flag;
    }

    public boolean at(int i) {
        return grammar.isTerminal(i) || finitary[i];
    }

    public void display(Writer writer) throws IOException {
        writer.write("Finitary = {");
        int i = 0;
        for (int j = 0; j < numNTs; j++) {
            if (!at(j)) {
                continue;
            }
            if (i > 0) {
                writer.write(", ");
            }
            writer.write(grammar.getSymbol(j).getName());
            i++;
        }
        writer.write("}\n");
    }
}

package org.xbib.jacc.grammar;

import java.io.IOException;
import java.io.Writer;

public class Nullable extends Analysis {

    private boolean nullable[];
    private boolean consider[];
    private Grammar grammar;
    private int numNTs;

    Nullable(Grammar grammar) {
        super(grammar.getComponents());
        this.grammar = grammar;
        numNTs = grammar.getNumNTs();
        nullable = new boolean[numNTs];
        consider = new boolean[numNTs];
        for (int i = 0; i < numNTs; i++) {
            nullable[i] = false;
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
                    nullable[i] = true;
                    consider[i] = false;
                    flag = true;
                    break;
                }
                if (grammar.isTerminal(ai[l]) || grammar.isNonterminal(ai[l]) && !consider[ai[l]]) {
                    j++;
                }
            }
            if (j == aprod.length) {
                consider[i] = false;
            }
        }
        return flag;
    }

    public boolean at(int i)
    {
        return grammar.isNonterminal(i) && nullable[i];
    }

    public void display(Writer writer) throws IOException {
        writer.write("Nullable = {");
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

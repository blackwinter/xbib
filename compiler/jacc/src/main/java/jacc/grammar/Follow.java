package jacc.grammar;

import jacc.util.BitSet;

import java.io.PrintWriter;

public final class Follow extends Analysis {

    private Grammar grammar;
    private Nullable nullable;
    private First first;
    private int numNTs;
    private int numTs;
    private int follow[][];

    public Follow(Grammar grammar1, Nullable nullable1, First first1) {
        super(grammar1.getComponents());
        grammar = grammar1;
        nullable = nullable1;
        first = first1;
        numNTs = grammar1.getNumNTs();
        numTs = grammar1.getNumTs();
        follow = new int[numNTs][];
        for (int i = 0; i < numNTs; i++) {
            follow[i] = BitSet.make(numTs);
        }

        BitSet.set(follow[0], numTs - 1);
        topDown();
    }

    protected boolean analyze(int i) {
        boolean flag = false;
        Grammar.Prod aprod[] = grammar.getProds(i);
        for (int j = 0; j < aprod.length; j++) {
            int ai[] = aprod[j].getRhs();
            for (int k = 0; k < ai.length; k++) {
                if (!grammar.isNonterminal(ai[k])) {
                    continue;
                }
                int l = k + 1;
                do {
                    if (l >= ai.length) {
                        break;
                    }
                    if (grammar.isTerminal(ai[l])) {
                        if (BitSet.addTo(follow[ai[k]], ai[l] - numNTs)) {
                            flag = true;
                        }
                        break;
                    }
                    if (BitSet.addTo(follow[ai[k]], first.at(ai[l]))) {
                        flag = true;
                    }
                    if (!nullable.at(ai[l])) {
                        break;
                    }
                    l++;
                } while (true);
                if (l >= ai.length && BitSet.addTo(follow[ai[k]], follow[i])) {
                    flag = true;
                }
            }

        }

        return flag;
    }

    public int[] at(int i) {
        return follow[i];
    }

    public void display(PrintWriter printwriter) {
        printwriter.println("Follow sets:");
        for (int i = 0; i < follow.length; i++) {
            printwriter.print(" Follow(" + grammar.getSymbol(i) + "): {");
            printwriter.print(grammar.displaySymbolSet(at(i), numNTs));
            printwriter.println("}");
        }

    }
}

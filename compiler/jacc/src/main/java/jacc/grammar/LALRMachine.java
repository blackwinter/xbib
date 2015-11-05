package jacc.grammar;

import jacc.util.BitSet;
import jacc.util.IntSet;
import jacc.util.SCC;

import java.io.PrintWriter;

public class LALRMachine extends LookaheadMachine {

    protected Nullable nullable;
    protected First first;
    private int predState[][];
    private int numGotos;
    private int stateFirstGoto[];
    private int gotoSource[];
    private int gotoTrans[];
    private int gotoLA[][];
    private int gotoTargets[][];
    private int laReds[][][];

    public LALRMachine(Grammar grammar) {
        super(grammar);
        nullable = grammar.getNullable();
        first = grammar.getFirst();
        predState = SCC.invert(succState, numStates);
        calcGotoLA();
        calcLookahead();
    }

    public int[] getLookaheadAt(int i, int j) {
        return laReds[i][j];
    }

    private void calcGotoLA() {
        stateFirstGoto = new int[numStates];
        numGotos = 0;
        for (int i = 0; i < numStates; i++) {
            stateFirstGoto[i] = numGotos;
            numGotos += getGotosAt(i).length;
        }

        gotoSource = new int[numGotos];
        gotoTrans = new int[numGotos];
        int j = 0;
        for (int k = 0; k < numStates; k++) {
            int ai1[] = getGotosAt(k);
            for (int j1 = 0; j1 < ai1.length; j1++) {
                gotoSource[j] = k;
                gotoTrans[j] = ai1[j1];
                j++;
            }

        }

        gotoLA = new int[numGotos][];
        gotoTargets = new int[numGotos][];
        for (int l = 0; l < numGotos; l++) {
            calcTargets(l);
        }

        int ai[][] = SCC.get(gotoTargets);
        for (int i1 = 0; i1 < ai.length; i1++) {
            int ai2[] = ai[i1];
            for (boolean flag = true; flag; ) {
                flag = false;
                int k1 = 0;
                while (k1 < ai2.length) {
                    int ai3[] = gotoTargets[ai2[k1]];
                    for (int l1 = 0; l1 < ai3.length; l1++) {
                        if (BitSet.addTo(gotoLA[ai2[k1]], gotoLA[ai3[l1]])) {
                            flag = true;
                        }
                    }
                    k1++;
                }
            }
        }
    }

    private void calcTargets(int i) {
        int j = gotoSource[i];
        int k = gotoTrans[i];
        int l = getEntry(k);
        IntSet intset = getItemsAt(k);
        int i1 = intset.size();
        int ai[] = BitSet.make(numTs);
        IntSet intset1 = IntSet.empty();
        for (int j1 = 0; j1 < i1; j1++) {
            LR0Items.Item item = items.getItem(intset.at(j1));
            int k1 = item.getLhs();
            int l1 = item.getPos();
            if (k1 >= 0) {
                int ai1[] = item.getProd().getRhs();
                if (l1 > 0 && ai1[--l1] == l && calcFirsts(ai, item).canReduce()) {
                    findTargets(intset1, j, k1, ai1, l1);
                }
                continue;
            }
            if (l1 > 0) {
                BitSet.set(ai, numTs - 1);
            }
        }

        gotoLA[i] = ai;
        gotoTargets[i] = intset1.toArray();
    }

    private LR0Items.Item calcFirsts(int ai[], LR0Items.Item item) {
        do {
            if (!item.canGoto()) {
                break;
            }
            int i = item.getNextSym();
            if (grammar.isTerminal(i)) {
                BitSet.addTo(ai, i - numNTs);
                break;
            }
            BitSet.union(ai, first.at(i));
            if (!nullable.at(i)) {
                break;
            }
            item = items.getItem(item.getNextItem());
        } while (true);
        if (item.canAccept()) {
            BitSet.set(ai, numTs - 1);
        }
        return item;
    }

    private void findTargets(IntSet intset, int i, int j, int ai[], int k) {
        if (k == 0) {
            int ai1[] = getGotosAt(i);
            int i1 = 0;
            do {
                if (i1 >= ai1.length) {
                    break;
                }
                if (getEntry(ai1[i1]) == j) {
                    intset.add(stateFirstGoto[i] + i1);
                    break;
                }
                i1++;
            } while (true);
        } else if (entry[i] == ai[--k]) {
            for (int l = 0; l < predState[i].length; l++) {
                findTargets(intset, predState[i][l], j, ai, k);
            }

        }
    }

    private void calcLookahead() {
        laReds = new int[numStates][][];
        for (int i = 0; i < numStates; i++) {
            int ai[] = getReducesAt(i);
            IntSet intset = getItemsAt(i);
            laReds[i] = new int[ai.length][];
            for (int j = 0; j < ai.length; j++) {
                LR0Items.Item item = items.getItem(intset.at(ai[j]));
                int k = item.getLhs();
                int ai1[] = item.getProd().getRhs();
                int ai2[] = BitSet.make(numTs);
                lookBack(ai2, i, k, ai1, ai1.length);
                laReds[i][j] = ai2;
            }

        }

    }

    private void lookBack(int ai[], int i, int j, int ai1[], int k) {
        if (k == 0) {
            int ai2[] = getGotosAt(i);
            for (int i1 = 0; i1 < ai2.length; i1++) {
                if (getEntry(ai2[i1]) == j) {
                    BitSet.union(ai, gotoLA[stateFirstGoto[i] + i1]);
                    return;
                }
            }

        } else if (entry[i] == ai1[--k]) {
            for (int l = 0; l < predState[i].length; l++) {
                lookBack(ai, predState[i][l], j, ai1, k);
            }

        }
    }

    public void display(PrintWriter writer) {
        super.display(writer);
        for (int i = 0; i < numGotos; i++) {
            writer.println("Goto #" + i + ", in state " + gotoSource[i] + " on symbol " + grammar.getSymbol(getEntry(gotoTrans[i])) + " to state " + gotoTrans[i]);
            writer.print("  Lookahead: {");
            writer.print(grammar.displaySymbolSet(gotoLA[i], numNTs));
            writer.println("}");
            writer.print("  Targets  : {");
            for (int k = 0; k < gotoTargets[i].length; k++) {
                if (k > 0) {
                    writer.print(", ");
                }
                writer.print(gotoTargets[i][k]);
            }
            writer.println("}");
        }

        for (int j = 0; j < numStates; j++) {
            int ai[] = getReducesAt(j);
            if (ai.length <= 0) {
                continue;
            }
            writer.println("State " + j + ": ");
            IntSet intset = getItemsAt(j);
            for (int l = 0; l < ai.length; l++) {
                LR0Items.Item item = items.getItem(intset.at(ai[l]));
                writer.print("  Item     : ");
                item.display(writer);
                writer.println();
                writer.print("  Lookahead: {");
                writer.print(grammar.displaySymbolSet(laReds[j][l], numNTs));
                writer.println("}");
            }

        }

    }
}

package org.xbib.jacc.grammar;

import org.xbib.jacc.util.BitSet;
import org.xbib.jacc.util.IntSet;
import org.xbib.jacc.util.Interator;

public class Tables {

    private static final byte NONE = 0;
    private static final byte SHIFT = 1;
    private static final byte REDUCE = 2;
    protected LookaheadMachine machine;
    private Resolver resolver;
    protected int numNTs;
    protected int numTs;
    protected byte action[][];
    protected int arg[][];
    private boolean prodUsed[][];
    private int prodUnused;

    public Tables(LookaheadMachine lookaheadmachine, Resolver resolver) {
        machine = lookaheadmachine;
        this.resolver = resolver;
        Grammar grammar = lookaheadmachine.getGrammar();
        numNTs = grammar.getNumNTs();
        numTs = grammar.getNumTs();
        int i = lookaheadmachine.getNumStates();
        action = new byte[i][];
        arg = new int[i][];
        prodUsed = new boolean[numNTs][];
        prodUnused = 0;
        for (int j = 0; j < numNTs; j++) {
            prodUsed[j] = new boolean[grammar.getProds(j).length];
            prodUnused += prodUsed[j].length;
        }
        for (int k = 0; k < i; k++) {
            fillTablesAt(k);
        }
    }

    public LookaheadMachine getMachine() {
        return machine;
    }

    public byte[] getActionAt(int i) {
        return action[i];
    }

    public int[] getArgAt(int i) {
        return arg[i];
    }

    public int getProdUnused() {
        return prodUnused;
    }

    public boolean[] getProdsUsedAt(int i) {
        return prodUsed[i];
    }

    private void setShift(int i, int j, int k) {
        action[i][j] = SHIFT;
        arg[i][j] = k;
    }

    public void setReduce(int i, int j, int k) {
        action[i][j] = REDUCE;
        arg[i][j] = k;
    }

    private void fillTablesAt(int i) {
        int ai1[];
        action[i] = new byte[numTs];
        arg[i] = new int[numTs];
        int ai[] = machine.getShiftsAt(i);
        ai1 = machine.getReducesAt(i);
        for (int anAi : ai) {
            setShift(i, machine.getEntry(anAi) - numNTs, anAi);
        }
        for (int k = 0; k < ai1.length; k++) {
            for(Interator interator = BitSet.interator(machine.getLookaheadAt(i, k), 0);
                interator.hasNext(); ) {
                int l = interator.next();
                switch (action[i][l]) {
                case NONE:
                    setReduce(i, l, ai1[k]);
                    break;
                case SHIFT:
                    resolver.srResolve(this, i, l, ai1[k]);
                    break;
                case REDUCE:
                    resolver.rrResolve(this, i, l, ai1[k]);
                    break;
                }
            }
        }
        LR0Items lr0items = machine.getItems();
        IntSet intset = machine.getItemsAt(i);
        for (int anAi1 : ai1) {
            for (int j1 = 0; j1 < numTs; j1++) {
                if (action[i][j1] == REDUCE && arg[i][j1] == anAi1) {
                    LR0Items.Item item = lr0items.getItem(intset.at(anAi1));
                    int k1 = item.getLhs();
                    int l1 = item.getProdNo();
                    if (!prodUsed[k1][l1]) {
                        prodUsed[k1][l1] = true;
                        prodUnused--;
                    }
                }
            }
        }
    }
}

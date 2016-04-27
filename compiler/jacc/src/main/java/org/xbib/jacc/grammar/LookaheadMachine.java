package org.xbib.jacc.grammar;

public abstract class LookaheadMachine extends Machine {

    LookaheadMachine(Grammar grammar) {
        super(grammar);
    }

    public abstract int[] getLookaheadAt(int i, int j);
}

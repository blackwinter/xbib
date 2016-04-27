package org.xbib.jacc;

import org.xbib.jacc.grammar.Grammar;
import org.xbib.jacc.grammar.LR0Items;
import org.xbib.jacc.grammar.LookaheadMachine;
import org.xbib.jacc.grammar.Resolver;
import org.xbib.jacc.grammar.Tables;
import org.xbib.jacc.util.IntSet;

class JaccResolver implements Resolver {

    private LookaheadMachine machine;
    private int numSRConflicts;
    private int numRRConflicts;
    private Conflicts conflicts[];

    JaccResolver(LookaheadMachine lookaheadmachine) {
        numSRConflicts = 0;
        numRRConflicts = 0;
        machine = lookaheadmachine;
        conflicts = new Conflicts[lookaheadmachine.getNumStates()];
    }

    int getNumSRConflicts() {
        return numSRConflicts;
    }

    int getNumRRConflicts() {
        return numRRConflicts;
    }

    String getConflictsAt(int i)
    {
        return Conflicts.describe(machine, i, conflicts[i]);
    }

    @Override
    public void srResolve(Tables tables, int i, int j, int k) {
        Grammar grammar = machine.getGrammar();
        Grammar.Symbol symbol = grammar.getTerminal(j);
        IntSet intset = machine.getItemsAt(i);
        LR0Items lr0items = machine.getItems();
        Grammar.Prod prod = lr0items.getItem(intset.at(k)).getProd();
        if ((symbol instanceof JaccSymbol) && (prod instanceof JaccProd)) {
            JaccSymbol jaccsymbol = (JaccSymbol)symbol;
            JaccProd jaccprod = (JaccProd)prod;
            switch (Fixity.which(jaccprod.getFixity(), jaccsymbol.getFixity())) {
            case 1:
                tables.setReduce(i, j, k);
                return;
            case 3:
                return;
            }
        }
        conflicts[i] = Conflicts.sr(tables.getArgAt(i)[j], k, symbol, conflicts[i]);
        numSRConflicts++;
    }

    @Override
    public void rrResolve(Tables tables, int i, int j, int k) {
        Grammar grammar = machine.getGrammar();
        int l = tables.getArgAt(i)[j];
        IntSet intset = machine.getItemsAt(i);
        LR0Items lr0items = machine.getItems();
        Grammar.Prod prod = lr0items.getItem(intset.at(l)).getProd();
        Grammar.Prod prod1 = lr0items.getItem(intset.at(k)).getProd();
        Grammar.Symbol symbol = grammar.getTerminal(j);
        if (prod1.getSeqNo() < prod.getSeqNo()) {
            tables.setReduce(i, j, k);
        }
        conflicts[i] = Conflicts.rr(l, k, symbol, conflicts[i]);
        numRRConflicts++;
    }
}

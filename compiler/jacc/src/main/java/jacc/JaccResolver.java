package jacc;

import jacc.grammar.Grammar;
import jacc.grammar.LR0Items;
import jacc.grammar.LookaheadMachine;
import jacc.grammar.Resolver;
import jacc.grammar.Tables;
import jacc.util.IntSet;

public class JaccResolver extends Resolver
{

    private LookaheadMachine machine;
    private int numSRConflicts;
    private int numRRConflicts;
    private Conflicts conflicts[];

    public JaccResolver(LookaheadMachine lookaheadmachine)
    {
        numSRConflicts = 0;
        numRRConflicts = 0;
        machine = lookaheadmachine;
        conflicts = new Conflicts[lookaheadmachine.getNumStates()];
    }

    public int getNumSRConflicts()
    {
        return numSRConflicts;
    }

    public int getNumRRConflicts()
    {
        return numRRConflicts;
    }

    public String getConflictsAt(int i)
    {
        return Conflicts.describe(machine, i, conflicts[i]);
    }

    public void srResolve(Tables tables, int i, int j, int k)
    {
        Grammar grammar = machine.getGrammar();
        jacc.grammar.Grammar.Symbol symbol = grammar.getTerminal(j);
        IntSet intset = machine.getItemsAt(i);
        LR0Items lr0items = machine.getItems();
        jacc.grammar.Grammar.Prod prod = lr0items.getItem(intset.at(k)).getProd();
        if ((symbol instanceof JaccSymbol) && (prod instanceof JaccProd))
        {
            JaccSymbol jaccsymbol = (JaccSymbol)symbol;
            JaccProd jaccprod = (JaccProd)prod;
            switch (Fixity.which(jaccprod.getFixity(), jaccsymbol.getFixity()))
            {
            case 1: // '\001'
                tables.setReduce(i, j, k);
                return;

            case 3: // '\003'
                return;
            }
        }
        conflicts[i] = Conflicts.sr(tables.getArgAt(i)[j], k, symbol, conflicts[i]);
        numSRConflicts++;
    }

    public void rrResolve(Tables tables, int i, int j, int k)
    {
        Grammar grammar = machine.getGrammar();
        int l = tables.getArgAt(i)[j];
        IntSet intset = machine.getItemsAt(i);
        LR0Items lr0items = machine.getItems();
        jacc.grammar.Grammar.Prod prod = lr0items.getItem(intset.at(l)).getProd();
        jacc.grammar.Grammar.Prod prod1 = lr0items.getItem(intset.at(k)).getProd();
        jacc.grammar.Grammar.Symbol symbol = grammar.getTerminal(j);
        if (prod1.getSeqNo() < prod.getSeqNo())
            tables.setReduce(i, j, k);
        conflicts[i] = Conflicts.rr(l, k, symbol, conflicts[i]);
        numRRConflicts++;
    }
}

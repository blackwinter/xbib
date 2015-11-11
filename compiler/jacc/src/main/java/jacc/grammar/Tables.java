package jacc.grammar;

import jacc.util.*;

public class Tables
{

    protected LookaheadMachine machine;
    protected Resolver resolver;
    protected int numNTs;
    protected int numTs;
    public static final byte NONE = 0;
    public static final byte SHIFT = 1;
    public static final byte REDUCE = 2;
    protected byte action[][];
    protected int arg[][];
    private boolean prodUsed[][];
    private int prodUnused;

    public Tables(LookaheadMachine lookaheadmachine, Resolver resolver1)
    {
        machine = lookaheadmachine;
        resolver = resolver1;
        Grammar grammar = lookaheadmachine.getGrammar();
        numNTs = grammar.getNumNTs();
        numTs = grammar.getNumTs();
        int i = lookaheadmachine.getNumStates();
        action = new byte[i][];
        arg = new int[i][];
        prodUsed = new boolean[numNTs][];
        prodUnused = 0;
        for (int j = 0; j < numNTs; j++)
        {
            prodUsed[j] = new boolean[grammar.getProds(j).length];
            prodUnused += prodUsed[j].length;
        }

        for (int k = 0; k < i; k++)
            fillTablesAt(k);

    }

    public LookaheadMachine getMachine()
    {
        return machine;
    }

    public byte[] getActionAt(int i)
    {
        return action[i];
    }

    public int[] getArgAt(int i)
    {
        return arg[i];
    }

    public int getProdUnused()
    {
        return prodUnused;
    }

    public boolean[] getProdsUsedAt(int i)
    {
        return prodUsed[i];
    }

    public void setShift(int i, int j, int k)
    {
        action[i][j] = 1;
        arg[i][j] = k;
    }

    public void setReduce(int i, int j, int k)
    {
        action[i][j] = 2;
        arg[i][j] = k;
    }

    private void fillTablesAt(int i)
    {
        int ai1[];
        //int k;
        action[i] = new byte[numTs];
        arg[i] = new int[numTs];
        int ai[] = machine.getShiftsAt(i);
        ai1 = machine.getReducesAt(i);
        for (int j = 0; j < ai.length; j++)
            setShift(i, machine.getEntry(ai[j]) - numNTs, ai[j]);

        for (int k = 0; k < ai1.length; k++)
        {
/*
        k = 0;
_L2:
        if (k >= ai1.length)
            break;

        Interator interator = BitSet.interator(machine.getLookaheadAt(i, k), 0);
        do
        {
label0:
            {
                if (!interator.hasNext())
                    break label0;
*/
            for(Interator interator = BitSet.interator(machine.getLookaheadAt(i, k), 0);
                interator.hasNext(); )
            {
                int l = interator.next();
                switch (action[i][l])
                {
                case 0: // '\0'
                    setReduce(i, l, ai1[k]);
                    break;

                case 1: // '\001'
                    resolver.srResolve(this, i, l, ai1[k]);
                    break;

                case 2: // '\002'
                    resolver.rrResolve(this, i, l, ai1[k]);
                    break;
                }
            }
            
/*
        } while (true);
        k++;
        if (true) goto _L2; else goto _L1
_L1:
*/
        }
        LR0Items lr0items = machine.getItems();
        IntSet intset = machine.getItemsAt(i);
/*label1:*/
        for (int i1 = 0; i1 < ai1.length; i1++)
        {
/*
            int j1 = 0;
            do
            {
                if (j1 >= numTs)
                    continue label1;
*/
            for (int j1 = 0; j1 < numTs; j1++)
            {
                if (action[i][j1] == 2 && arg[i][j1] == ai1[i1])
                {
                    LR0Items.Item item = lr0items.getItem(intset.at(ai1[i1]));
                    int k1 = item.getLhs();
                    int l1 = item.getProdNo();
                    if (!prodUsed[k1][l1])
                    {
                        prodUsed[k1][l1] = true;
                        prodUnused--;
                    }
                    /*continue label1;*/
                }
/*
                j1++;
            } while (true);
*/
            }
        }

        return;
    }
}

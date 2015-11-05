package jacc.grammar;

import jacc.util.*;
import java.io.PrintWriter;

public class Machine
{

    protected Grammar grammar;
    protected int numSyms;
    protected int numNTs;
    protected int numTs;
    protected Left left;
    protected LR0Items items;
    protected int numStates;
    protected IntSet stateSets[];
    protected int entry[];
    private IntSet nullReds[];
    protected int succState[][];
    protected int gotos[][];
    protected int shifts[][];
    protected int reduceOffsets[][];
    private final int DEFAULT_NUM_STATES = 16;
    private final IntSet acceptItems = IntSet.singleton(-1);

    public Machine(Grammar grammar1)
    {
        grammar = grammar1;
        numSyms = grammar1.getNumSyms();
        numNTs = grammar1.getNumNTs();
        numTs = grammar1.getNumTs();
        left = grammar1.getLeft();
        items = new LR0Items(grammar1);
        calcLR0states();
        calcGotosShifts();
        calcReduceOffsets();
    }

    public Grammar getGrammar()
    {
        return grammar;
    }

    public int getNumStates()
    {
        return numStates;
    }

    public LR0Items getItems()
    {
        return items;
    }

    public LR0Items.Item reduceItem(int i, int j)
    {
        return items.getItem(stateSets[i].at(j));
    }

    public int getEntry(int i)
    {
        return i >= 0 ? entry[i] : numSyms - 1;
    }

    public IntSet getItemsAt(int i)
    {
        return stateSets[i];
    }

    public int[] getGotosAt(int i)
    {
        return gotos[i];
    }

    public int[] getShiftsAt(int i)
    {
        return shifts[i];
    }

    public int[] getReducesAt(int i)
    {
        return reduceOffsets[i];
    }

    private void calcLR0states()
    {
        stateSets = new IntSet[16];
        succState = new int[16][];
        entry = new int[16];
        nullReds = new IntSet[16];
        stateSets[0] = IntSet.singleton(items.getStartItem());
        numStates = 1;
        IntSet aintset[] = new IntSet[numSyms];
        int i = 0;
        int ai[] = BitSet.make(numNTs);
        for (int j = 0; j < numStates; j++)
        {
            IntSet intset = stateSets[j];
            BitSet.clear(ai);
            Interator interator = intset.interator();
            do
            {
                if (!interator.hasNext())
                    break;
                LR0Items.Item item = items.getItem(interator.next());
                if (item.canGoto())
                {
                    int k = item.getNextSym();
                    int i1 = item.getNextItem();
                    if (grammar.isNonterminal(k))
                        BitSet.addTo(ai, left.at(k));
                    if (addValue(aintset, k, i1))
                        i++;
                }
            } while (true);
            if (!BitSet.isEmpty(ai))
            {
                for (Interator interator1 = BitSet.interator(ai, 0); interator1.hasNext();)
                {
                    int l = interator1.next();
                    Grammar.Prod aprod[] = grammar.getProds(l);
                    int k1 = 0;
                    while (k1 < aprod.length) 
                    {
                        int ai3[] = aprod[k1].getRhs();
                        int i2 = items.getFirstKernel(l, k1);
                        if (ai3.length != 0)
                        {
                            if (addValue(aintset, ai3[0], i2))
                                i++;
                        } else
                        {
                            addValue(nullReds, j, i2);
                        }
                        k1++;
                    }
                }

            }
            int ai1[] = new int[i];
            int ai2[] = new int[i];
            int j1 = 0;
            for (int l1 = 0; j1 < i; l1++)
                if (aintset[l1] != null)
                {
                    ai1[j1] = addState(l1, aintset[l1]);
                    ai2[j1] = l1;
                    aintset[l1] = null;
                    j1++;
                }

            i = 0;
            succState[j] = ai1;
        }

        mergeNullReds();
    }

    private boolean addValue(IntSet aintset[], int i, int j)
    {
        if (aintset[i] == null)
        {
            aintset[i] = IntSet.singleton(j);
            return true;
        } else
        {
            aintset[i].add(j);
            return false;
        }
    }

    private int addState(int i, IntSet intset)
    {
        for (int j = 0; j < numStates; j++)
            if (stateSets[j].equals(intset))
                return j;

        if (acceptItems.equals(intset))
            return -1;
        if (numStates >= stateSets.length)
        {
            int k = 2 * stateSets.length;
            IntSet aintset[] = new IntSet[k];
            int ai[][] = new int[k][];
            IntSet aintset1[] = new IntSet[k];
            int ai1[] = new int[k];
            for (int l = 0; l < numStates; l++)
            {
                aintset[l] = stateSets[l];
                ai[l] = succState[l];
                ai1[l] = entry[l];
                aintset1[l] = nullReds[l];
            }

            stateSets = aintset;
            succState = ai;
            entry = ai1;
            nullReds = aintset1;
        }
        stateSets[numStates] = intset;
        entry[numStates] = i;
        return numStates++;
    }

    private void mergeNullReds()
    {
        for (int i = 0; i < numStates; i++)
        {
            if (nullReds[i] == null)
                continue;
            for (Interator interator = nullReds[i].interator(); interator.hasNext(); stateSets[i].add(interator.next()));
            nullReds[i] = null;
        }

    }

    private void calcGotosShifts()
    {
        gotos = new int[numStates][];
        shifts = new int[numStates][];
        for (int i = 0; i < numStates; i++)
        {
            int j = 0;
            int k = 0;
            for (int l = 0; l < succState[i].length; l++)
            {
                int j1 = succState[i][l];
                if (grammar.isTerminal(entry[j1]))
                    k++;
                else
                    j++;
            }

            if (stateSets[i].contains(items.getEndItem()))
                k++;
            gotos[i] = new int[j];
            shifts[i] = new int[k];
            for (int i1 = succState[i].length; 0 < i1--;)
            {
                int k1 = succState[i][i1];
                if (grammar.isTerminal(entry[k1]))
                    shifts[i][--k] = k1;
                else
                    gotos[i][--j] = k1;
            }

            if (k > 0)
                shifts[i][0] = -1;
        }

    }

    private void calcReduceOffsets()
    {
        reduceOffsets = new int[numStates][];
        for (int i = 0; i < numStates; i++)
        {
            int j = 0;
            IntSet intset = stateSets[i];
            int k = intset.size();
            for (int l = 0; l < k; l++)
                if (items.getItem(intset.at(l)).canReduce())
                    j++;

            reduceOffsets[i] = new int[j];
            int i1 = 0;
            for (int j1 = 0; j1 < k; j1++)
                if (items.getItem(intset.at(j1)).canReduce())
                    reduceOffsets[i][i1++] = j1;

        }

    }

    public void display(PrintWriter printwriter)
    {
        for (int i = 0; i < numStates; i++)
        {
            printwriter.println("state " + i);
            for (Interator interator = stateSets[i].interator(); interator.hasNext(); printwriter.println())
            {
                printwriter.print("\t");
                items.getItem(interator.next()).display(printwriter);
            }

            printwriter.println();
            if (succState[i].length <= 0)
                continue;
            for (int j = 0; j < succState[i].length; j++)
            {
                int k = succState[i][j];
                printwriter.println("\t" + grammar.getSymbol(entry[k]) + " goto " + succState[i][j]);
            }

            printwriter.println();
        }

    }
}

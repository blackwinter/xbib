package jacc;

import compiler.Handler;
import jacc.grammar.First;
import jacc.grammar.Follow;
import jacc.grammar.Grammar;
import jacc.grammar.LR0Items;
import jacc.grammar.Machine;
import jacc.grammar.Nullable;
import jacc.util.IntSet;
import java.io.PrintWriter;

public class TextOutput extends Output
{

    private boolean wantFirst;

    public TextOutput(Handler handler, JaccJob jaccjob, boolean flag)
    {
        super(handler, jaccjob);
        wantFirst = false;
        wantFirst = flag;
        tables.analyzeRows();
    }

    public void write(PrintWriter printwriter)
    {
        datestamp(printwriter);
        for (int i = 0; i < numStates; i++)
        {
            printwriter.print(resolver.getConflictsAt(i));
            printwriter.println(describeEntry(i));
            IntSet intset = machine.getItemsAt(i);
            int k = intset.size();
            for (int i1 = 0; i1 < k; i1++)
            {
                indent(printwriter, 1);
                machine.getItems().getItem(intset.at(i1)).display(printwriter);
                printwriter.println();
            }

            printwriter.println();
            byte abyte0[] = tables.getActionAt(i);
            int ai1[] = tables.getArgAt(i);
            int j1 = tables.getDefaultRowAt(i);
            int ai2[] = tables.indexAt(i);
            for (int k1 = 0; k1 < abyte0.length; k1++)
            {
                int l1 = ai2[k1];
                if (j1 < 0 || abyte0[l1] != abyte0[j1] || ai1[l1] != ai1[j1])
                {
                    indent(printwriter, 1);
                    printwriter.print(grammar.getTerminal(l1).getName());
                    printwriter.print(' ');
                    printwriter.println(describeAction(i, abyte0[l1], ai1[l1]));
                }
            }

            indent(printwriter, 1);
            if (j1 < 0)
            {
                printwriter.println(". error");
            } else
            {
                printwriter.print(". ");
                printwriter.println(describeAction(i, abyte0[j1], ai1[j1]));
            }
            printwriter.println();
            int ai3[] = machine.getGotosAt(i);
            if (ai3.length <= 0)
                continue;
            for (int i2 = 0; i2 < ai3.length; i2++)
            {
                int j2 = machine.getEntry(ai3[i2]);
                int k2 = ai3[i2];
                indent(printwriter, 1);
                printwriter.print(grammar.getSymbol(j2).getName());
                printwriter.println(" " + describeGoto(k2));
            }

            printwriter.println();
        }

        if (wantFirst)
        {
            grammar.getFirst().display(printwriter);
            printwriter.println();
            grammar.getFollow().display(printwriter);
            printwriter.println();
            grammar.getNullable().display(printwriter);
            printwriter.println();
        }
        if (tables.getProdUnused() > 0)
        {
            for (int j = 0; j < numNTs; j++)
            {
                boolean aflag[] = tables.getProdsUsedAt(j);
                for (int l = 0; l < aflag.length; l++)
                    if (!aflag[l])
                    {
                        int ai[] = grammar.getProds(j)[l].getRhs();
                        printwriter.print("Rule not reduced: ");
                        printwriter.print(grammar.getNonterminal(j).getName());
                        printwriter.print(" : ");
                        printwriter.println(grammar.displaySymbols(ai, "", " "));
                    }

            }

            printwriter.println();
        }
        printwriter.println(numTs + " terminals, " + numNTs + " nonterminals;");
        printwriter.println(grammar.getNumProds() + " grammar rules, " + numStates + " states;");
        printwriter.println(resolver.getNumSRConflicts() + " shift/reduce and " + resolver.getNumRRConflicts() + " reduce/reduce conflicts reported.");
    }

    protected String describeEntry(int i)
    {
        return "state " + i + " (entry on " + grammar.getSymbol(machine.getEntry(i)) + ")";
    }

    private String describeAction(int i, int j, int k)
    {
        if (j == 0)
            if (k == 0)
                return "error";
            else
                return "error \"" + tables.getError(k - 1) + "\"";
        if (j == 2)
            return "reduce " + machine.reduceItem(i, k).getSeqNo();
        if (k < 0)
            return "accept";
        else
            return describeShift(k);
    }

    protected String describeShift(int i)
    {
        return "shift " + i;
    }

    protected String describeGoto(int i)
    {
        return "goto " + i;
    }
}

package jacc;

import jacc.grammar.LookaheadMachine;
import jacc.grammar.Resolver;
import jacc.grammar.Tables;
import java.io.PrintWriter;

public class JaccTables extends Tables
{
    private class RowAnalysis
    {

        private byte a[];
        private int b[];
        private int size;
        private int idx[];

        public void analyze(int i)
        {
            a = action[i];
            b = arg[i]; 
            size = numTs;
            idx = new int[size];
            for (int j = 0; j < numTs; j++)
                idx[j] = j;

            for (int k = size / 2; k >= 0; k--)
                heapify(k);

            for (int l = size - 1; l > 0; l--)
            {
                int i1 = idx[l];
                idx[l] = idx[0];
                idx[0] = i1;
                size--;
                heapify(0);
            }

            index[i] = idx;
            defaultRow[i] = findDefault();
        }

        private void heapify(int i)
        {
            int j = i;
            int k = idx[j];
            do
            {
                int l = 2 * i + 1;
                int i1 = l + 1;
                if (l < size)
                {
                    int j1 = idx[l];
                    if (a[j1] > a[k] || a[j1] == a[k] && b[j1] > b[k])
                    {
                        j = l;
                        k = j1;
                    }
                    if (i1 < size)
                    {
                        int k1 = idx[i1];
                        if (a[k1] > a[k] || a[k1] == a[k] && b[k1] > b[k])
                        {
                            j = i1;
                            k = k1;
                        }
                    }
                }
                if (j == i)
                    return;
                idx[j] = idx[i];
                idx[i] = k;
                i = j;
                k = idx[j];
            } while (true);
        }

        public int findDefault()
        {
            int i = 1;
            int j = -1;
            int k = 0;
            do
            {
                if (k >= a.length)
                    break;
                int l = idx[k];
                byte byte0 = a[l];
                if (byte0 == 1)
                {
                    k++;
                } else
                {
                    int i1 = l;
                    int j1 = 1;
                    for (int k1 = b[l]; ++k < a.length && a[idx[k]] == byte0 && b[idx[k]] == k1;)
                        j1++;

                    if (j1 > i)
                    {
                        j = l;
                        i = j1;
                    }
                }
            } while (true);
            return j;
        }

        private void display()
        {
            for (int i = 0; i < numTs; i++)
                display(idx[i]);

            System.out.println();
        }

        private void display(int i)
        {
            switch (a[i])
            {
            case 0: // '\0'
                System.out.print(" E");
                break;

            case 1: // '\001'
                System.out.print(" S");
                break;

            case 2: // '\002'
                System.out.print(" R");
                break;
            }
            System.out.print(b[i]);
        }

        private RowAnalysis()
        {
        }

    }


    private String errors[];
    private int numErrors;
    private int index[][];
    private int defaultRow[];

    public JaccTables(LookaheadMachine lookaheadmachine, Resolver resolver)
    {
        super(lookaheadmachine, resolver);
        errors = null;
        numErrors = 0;
    }

    public int getNumErrors()
    {
        return numErrors;
    }

    public String getError(int i)
    {
        return errors[i];
    }

    public boolean errorAt(int i, int j)
    {
        return action[i][j - numNTs] == 0;
    }

    public String errorSet(int i, int j, String s)
    {
        if (arg[i][j - numNTs] != 0)
        {
            return errors[arg[i][j - numNTs] - 1];
        } else
        {
            arg[i][j - numNTs] = errorNo(s) + 1;
            return null;
        }
    }

    private int errorNo(String s)
    {
        for (int i = 0; i < numErrors; i++)
            if (errors[i].equals(s))
                return i;

        String as[] = new String[numErrors != 0 ? 2 * numErrors : 1];
        for (int j = 0; j < numErrors; j++)
            as[j] = errors[j];

        errors = as;
        errors[numErrors] = s;
        return numErrors++;
    }

    public void analyzeRows()
    {
        if (index == null)
        {
            RowAnalysis rowanalysis = new RowAnalysis();
            int i = machine.getNumStates();
            index = new int[i][];
            defaultRow = new int[i];
            for (int j = 0; j < i; j++)
                rowanalysis.analyze(j);

        }
    }

    public int[] indexAt(int i)
    {
        return index[i];
    }

    public int getDefaultRowAt(int i)
    {
        return defaultRow[i];
    }

    public void display(PrintWriter printwriter)
    {
        int i = machine.getNumStates();
        for (int j = 0; j < i; j++)
        {
            System.out.print("state " + j + ": ");
            for (int k = 0; k < numTs; k++)
            {
                switch (action[j][k])
                {
                case 0: // '\0'
                    printwriter.print(" E");
                    break;

                case 1: // '\001'
                    printwriter.print(" S");
                    break;

                case 2: // '\002'
                    printwriter.print(" R");
                    break;
                }
                printwriter.print(arg[j][k]);
            }

            printwriter.println();
        }

    }


}

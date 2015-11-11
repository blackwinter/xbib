package jacc.util;

import java.io.PrintWriter;

public class SCC
{
    private static class GetComponents extends DepthFirst
    {

        private int numComps;
        private int compNo[];

        void doneVisit(int i)
        {
            compNo[i] = numComps;
        }

        void doneTree()
        {
            numComps++;
        }

        int[][] getComponents()
        {
            search();
            int ai[] = new int[numComps];
            for (int i = 0; i < compNo.length; i++)
                ai[compNo[i]]++;

            int ai1[][] = new int[numComps][];
            for (int j = 0; j < numComps; j++)
                ai1[j] = new int[ai[j]];

            for (int k = 0; k < compNo.length; k++)
            {
                int l = compNo[k];
                ai1[l][--ai[l]] = k;
            }

            return ai1;
        }

        GetComponents(int ai[][], int i, int ai1[])
        {
            super(new ElemInterator(ai1), ai);
            numComps = 0;
            compNo = new int[i];
        }
    }

    private static class ArrangeByFinish extends DepthFirst
    {

        private int dfsNum;
        private int order[];

        void doneVisit(int i)
        {
            order[--dfsNum] = i;
        }

        int[] getFinishOrder()
        {
            search();
            return order;
        }

        ArrangeByFinish(int ai[][], int i)
        {
            super(new SeqInterator(0, i), ai);
            dfsNum = i;
            order = new int[dfsNum];
        }
    }


    public SCC()
    {
    }

    public static int[][] get(int ai[][], int ai1[][], int i)
    {
        return (new GetComponents(ai, i, (new ArrangeByFinish(ai1, i)).getFinishOrder())).getComponents();
    }

    public static int[][] get(int ai[][])
    {
        return get(ai, invert(ai), ai.length);
    }

    public static int[][] get(int ai[][], int i)
    {
        return get(ai, invert(ai, i), i);
    }

    public static int[][] invert(int ai[][])
    {
        return invert(ai, ai.length);
    }

    public static int[][] invert(int ai[][], int i)
    {
        int ai1[] = new int[i];
        for (int j = 0; j < i; j++)
        {
            for (int k = 0; k < ai[j].length; k++)
                ai1[ai[j][k]]++;

        }

        int ai2[][] = new int[i][];
        for (int l = 0; l < i; l++)
            ai2[l] = new int[ai1[l]];

        for (int i1 = 0; i1 < i; i1++)
        {
            for (int j1 = 0; j1 < ai[i1].length; j1++)
            {
                int k1 = ai[i1][j1];
                ai1[k1]--;
                ai2[k1][ai1[k1]] = i1;
            }

        }

        return ai2;
    }

    public static void displayComponents(PrintWriter printwriter, int ai[][])
    {
        printwriter.println("Components (" + ai.length + " in total):");
        for (int i = 0; i < ai.length; i++)
        {
            printwriter.print(" Component " + i + ": {");
            for (int j = 0; j < ai[i].length; j++)
            {
                if (j > 0)
                    printwriter.print(", ");
                printwriter.print(ai[i][j]);
            }

            printwriter.println("}");
        }

    }
}

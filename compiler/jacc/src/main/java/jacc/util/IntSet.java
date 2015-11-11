package jacc.util;

import java.io.PrintWriter;

public class IntSet
{

    private int elems[];
    private int used;
    private static final int START_SIZE = 1;

    private IntSet()
    {
        elems = new int[1];
        used = 0;
    }

    public static IntSet empty()
    {
        return new IntSet();
    }

    public static IntSet singleton(int i)
    {
        IntSet intset = new IntSet();
        intset.add(i);
        return intset;
    }

    public int size()
    {
        return used;
    }

    public boolean isEmpty()
    {
        return used == 0;
    }

    public void clear()
    {
        used = 0;
    }

    public int at(int i)
    {
        return elems[i];
    }

    public int[] toArray()
    {
        int ai[] = new int[used];
        for (int i = 0; i < used; i++)
            ai[i] = elems[i];

        return ai;
    }

    public boolean contains(int i)
    {
        int j = 0;
        for (int k = used; j < k;)
        {
            int l = (j + k) / 2;
            int i1 = elems[l];
            if (i == i1)
                return true;
            if (i < i1)
                k = l;
            else
                j = l + 1;
        }

        return false;
    }

    public void add(int i)
    {
        int j = 0;
        for (int k = used; j < k;)
        {
            int l = (j + k) / 2;
            int j1 = elems[l];
            if (i < j1)
            {
                k = l;
            } else
            {
                if (i == j1)
                    return;
                j = l + 1;
            }
        }

        if (used >= elems.length)
        {
            int ai[] = new int[elems.length * 2];
            for (int k1 = 0; k1 < j; k1++)
                ai[k1] = elems[k1];

            ai[j] = i;
            for (int l1 = j; l1 < used; l1++)
                ai[l1 + 1] = elems[l1];

            elems = ai;
        } else
        {
            for (int i1 = used; i1 > j; i1--)
                elems[i1] = elems[i1 - 1];

            elems[j] = i;
        }
        used++;
    }

    public boolean equals(IntSet intset)
    {
        if (used == intset.used)
        {
            for (int i = 0; i < used; i++)
                if (elems[i] != intset.elems[i])
                    return false;

            return true;
        } else
        {
            return false;
        }
    }

    public Interator interator()
    {
        return new ElemInterator(elems, 0, used);
    }

    public static void main(String args[])
    {
        PrintWriter printwriter = new PrintWriter(System.out);
        IntSet intset = empty();
        int ai[] = {
            4, 3, 7, 3, 1, 6, 8, 7, 7, 9, 
            5, 5, 2, 0
        };
        for (int i = 0; i < ai.length; i++)
        {
            intset.display(printwriter);
            printwriter.println("adding " + ai[i]);
            intset.add(ai[i]);
        }

        intset.display(printwriter);
    }

    public void display(PrintWriter printwriter)
    {
        Interator interator1 = interator();
        printwriter.print("{");
        for (int i = 0; interator1.hasNext(); i++)
        {
            if (i != 0)
                printwriter.print(", ");
            printwriter.print(interator1.next());
        }

        printwriter.print("}");
        printwriter.println(": used = " + used + ", length = " + elems.length);
    }
}

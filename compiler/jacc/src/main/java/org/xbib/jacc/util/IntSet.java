package org.xbib.jacc.util;

import java.io.IOException;
import java.io.Writer;

public class IntSet {

    private int elems[];
    private int used;

    private IntSet() {
        elems = new int[1];
        used = 0;
    }

    public static IntSet empty()
    {
        return new IntSet();
    }

    public static IntSet singleton(int i) {
        IntSet intset = new IntSet();
        intset.add(i);
        return intset;
    }

    public int size()
    {
        return used;
    }

    public int at(int i)
    {
        return elems[i];
    }

    public int[] toArray() {
        int[] ai = new int[used];
        System.arraycopy(elems, 0, ai, 0, used);
        return ai;
    }

    public boolean contains(int i) {
        int j = 0;
        for (int k = used; j < k;) {
            int l = (j + k) / 2;
            int i1 = elems[l];
            if (i == i1) {
                return true;
            }
            if (i < i1) {
                k = l;
            } else {
                j = l + 1;
            }
        }
        return false;
    }

    public void add(int i) {
        int j = 0;
        for (int k = used; j < k;) {
            int l = (j + k) / 2;
            int j1 = elems[l];
            if (i < j1) {
                k = l;
            } else {
                if (i == j1)
                    return;
                j = l + 1;
            }
        }
        if (used >= elems.length) {
            int[] ai = new int[elems.length * 2];
            System.arraycopy(elems, 0, ai, 0, j);
            ai[j] = i;
            System.arraycopy(elems, j, ai, j + 1, used - j);
            elems = ai;
        } else {
            System.arraycopy(elems, j, elems, j + 1, used - j);
            elems[j] = i;
        }
        used++;
    }

    public boolean equals(IntSet intset) {
        if (used == intset.used) {
            for (int i = 0; i < used; i++) {
                if (elems[i] != intset.elems[i]) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    public Interator interator()
    {
        return new ElemInterator(elems, 0, used);
    }

    public void display(Writer writer) throws IOException {
        Interator interator1 = interator();
        writer.write("{");
        for (int i = 0; interator1.hasNext(); i++) {
            if (i != 0) {
                writer.write(", ");
            }
            writer.write(interator1.next());
        }
        writer.write("}");
        writer.write(": used = " + used + ", length = " + elems.length + "\n");
    }
}

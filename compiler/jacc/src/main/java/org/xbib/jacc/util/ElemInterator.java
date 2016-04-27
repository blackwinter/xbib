package org.xbib.jacc.util;

class ElemInterator extends Interator {

    private int count;
    private int limit;
    private int[] a;

    ElemInterator(int[] ai, int i, int j) {
        a = ai;
        count = i;
        limit = j;
    }

    ElemInterator(int[] ai)
    {
        this(ai, 0, ai.length);
    }

    public int next()
    {
        return a[count++];
    }

    public boolean hasNext()
    {
        return count < limit;
    }
}

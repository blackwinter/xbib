package org.xbib.jacc.util;

class SeqInterator extends Interator {

    private int count;
    private int limit;

    SeqInterator(int i, int j) {
        count = i;
        limit = j;
    }

    public int next() {
        return count++;
    }

    public boolean hasNext() {
        return count < limit;
    }
}

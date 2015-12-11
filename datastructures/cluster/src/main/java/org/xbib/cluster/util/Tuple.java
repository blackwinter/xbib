package org.xbib.cluster.util;

public class Tuple<A, B> {
    private final A a;

    private final B b;

    public Tuple(A a, B b) {
        this.a = a;
        this.b = b;
    }

    public A a() {
        return a;
    }

    public B b() {
        return b;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Tuple)) {
            return false;
        }
        Tuple tuple = (Tuple) o;
        return !(a != null ? !a.equals(tuple.a) : tuple.a != null) && !(b != null ? !b.equals(tuple.b) : tuple.b != null);
    }

    @Override
    public int hashCode() {
        int result = a != null ? a.hashCode() : 0;
        result = 31 * result + (b != null ? b.hashCode() : 0);
        return result;
    }
}

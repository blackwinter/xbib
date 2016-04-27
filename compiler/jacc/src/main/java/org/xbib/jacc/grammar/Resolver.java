package org.xbib.jacc.grammar;

public interface Resolver {

    void srResolve(Tables tables, int i, int j, int k);

    void rrResolve(Tables tables, int i, int j, int k);
}

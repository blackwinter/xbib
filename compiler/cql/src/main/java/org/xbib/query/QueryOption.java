package org.xbib.query;

public interface QueryOption<V> {

    void setName(String name);

    String getName();

    void setValue(V value);

    V getValue();
}

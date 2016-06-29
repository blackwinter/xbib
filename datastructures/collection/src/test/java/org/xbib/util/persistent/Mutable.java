package org.xbib.util.persistent;

public interface Mutable<T> {
    T getValue();

    void setValue(T value);
}
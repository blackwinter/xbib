package org.xbib.util;

public interface Mutable<T> {
    T getValue();

    void setValue(T value);
}
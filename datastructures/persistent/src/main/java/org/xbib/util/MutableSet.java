package org.xbib.util;

import java.util.Set;

public interface MutableSet<E> extends Set<E> {

    boolean addAllFrom(Iterable<E> iterable);

    PersistentSet<E> toPersistentSet();

}
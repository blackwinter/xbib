package org.xbib.cluster.service.ringmap;

public interface MapMergePolicy<V> {
    public V merge(V first, V other);
}

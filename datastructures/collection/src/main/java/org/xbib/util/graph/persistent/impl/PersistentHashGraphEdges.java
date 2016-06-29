package org.xbib.util.graph.persistent.impl;

import org.xbib.util.graph.persistent.GraphEdges;
import org.xbib.util.graph.persistent.IntMap;
import org.xbib.util.graph.persistent.ObjectSet;
import org.xbib.util.graph.persistent.internal.InternalGraphEdges;

import java.util.Collection;

public class PersistentHashGraphEdges<E> extends InternalGraphEdges<E> {

    public PersistentHashGraphEdges() {
        this(new PersistentHashIntMap<ObjectSet<E>>());
    }

    public PersistentHashGraphEdges(IntMap<ObjectSet<E>> edges) {
        super(edges);
    }

    @Override
    public GraphEdges<E> createEdges(IntMap<ObjectSet<E>> newEdges) {
        return new PersistentHashGraphEdges<E>(newEdges);
    }

    @Override
    public ObjectSet<E> createSet(E label) {
        return new PersistentObjectSet<E>(label);
    }

    @Override
    public ObjectSet<E> createSet(Collection<? extends E> labels) {
        return new PersistentObjectSet<E>(labels);
    }
}

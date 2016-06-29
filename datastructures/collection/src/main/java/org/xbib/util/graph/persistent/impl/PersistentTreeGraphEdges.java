package org.xbib.util.graph.persistent.impl;

import org.xbib.util.graph.persistent.GraphEdges;
import org.xbib.util.graph.persistent.IntMap;
import org.xbib.util.graph.persistent.ObjectSet;
import org.xbib.util.graph.persistent.internal.InternalGraphEdges;

public class PersistentTreeGraphEdges<E> extends InternalGraphEdges<E> {

    public PersistentTreeGraphEdges() {
        this(new PersistentTreeIntMap<>());
    }

    public PersistentTreeGraphEdges(IntMap<ObjectSet<E>> edges) {
        super(edges);
    }

    @Override
    public GraphEdges<E> createEdges(IntMap<ObjectSet<E>> newEdges) {
        return new PersistentTreeGraphEdges<E>(newEdges);
    }

    @Override
    public ObjectSet<E> createSet(E label) {
        return new PersistentObjectSet<E>(label);
    }

}

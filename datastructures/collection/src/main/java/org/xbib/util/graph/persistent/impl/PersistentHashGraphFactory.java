package org.xbib.util.graph.persistent.impl;

import org.xbib.util.graph.persistent.Graph;
import org.xbib.util.graph.persistent.GraphBuilder;
import org.xbib.util.graph.persistent.GraphFactory;

public class PersistentHashGraphFactory<V, E> implements GraphFactory<V, E> {

    @Override
    public Graph<V, E> of() {
        return new PersistentHashGraph<V, E>();
    }

    @Override
    public GraphBuilder<V, E> builder() {
        return new PersistentHashGraphBuilder<V, E>();
    }
}

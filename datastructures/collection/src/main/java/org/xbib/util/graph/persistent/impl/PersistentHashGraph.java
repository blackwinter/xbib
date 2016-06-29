package org.xbib.util.graph.persistent.impl;

import org.xbib.util.graph.persistent.Graph;
import org.xbib.util.graph.persistent.GraphNode;
import org.xbib.util.graph.persistent.IntMap;
import org.xbib.util.graph.persistent.internal.InternalGraph;

public class PersistentHashGraph<V, E> extends InternalGraph<V, E> {

    public PersistentHashGraph() {
        this(new PersistentHashIntMap<GraphNode<V, E>>());
    }

    public PersistentHashGraph(IntMap<GraphNode<V, E>> contexts) {
        super(contexts);
    }

    @Override
    public Graph<V, E> createGraph(IntMap<GraphNode<V, E>> contexts) {
        return new PersistentHashGraph<V, E>(contexts);
    }

    @Override
    public GraphNode<V, E> createNode(int node, V nodeLabel) {
        return new PersistentGraphNode<V, E>(new PersistentHashGraphEdges<E>(), node, nodeLabel, new PersistentHashGraphEdges<E>());
    }
}

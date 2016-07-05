package org.xbib.util.graph.persistent.impl;

import org.xbib.util.graph.persistent.Graph;
import org.xbib.util.graph.persistent.GraphNode;
import org.xbib.util.graph.persistent.IntMap;
import org.xbib.util.graph.persistent.internal.InternalGraph;

public class PersistentTreeGraph<V, E> extends InternalGraph<V, E> {

    public PersistentTreeGraph() {
        this(new PersistentTreeIntMap<GraphNode<V, E>>());
    }

    public PersistentTreeGraph(IntMap<GraphNode<V, E>> contexts) {
        super(contexts);
    }

    @Override
    public Graph<V, E> createGraph(IntMap<GraphNode<V, E>> contexts) {
        return new PersistentTreeGraph<V, E>(contexts);
    }

    @Override
    public GraphNode<V, E> createNode(int node, V nodeLabel) {
        return new PersistentGraphNode<V, E>(new PersistentTreeGraphEdges<E>(), node, nodeLabel, new PersistentTreeGraphEdges<E>());
    }
}

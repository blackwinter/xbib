package org.xbib.util.graph.persistent.impl;

import org.xbib.util.graph.persistent.GraphEdges;
import org.xbib.util.graph.persistent.GraphNode;
import org.xbib.util.graph.persistent.internal.InternalGraphNode;

public class PersistentGraphNode<V, E> extends InternalGraphNode<V, E> {

    public PersistentGraphNode(GraphEdges<E> inc, int node, V nodeLabel, GraphEdges<E> out) {
        super(inc, node, nodeLabel, out);
    }

    @Override
    public GraphNode<V, E> modify(GraphEdges<E> newInc, GraphEdges<E> newOut) {
        return new PersistentGraphNode<V, E>(newInc, node(), label(), newOut);
    }
}

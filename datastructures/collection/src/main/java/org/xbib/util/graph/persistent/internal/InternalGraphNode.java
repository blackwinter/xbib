package org.xbib.util.graph.persistent.internal;

import org.xbib.util.graph.persistent.GraphEdges;
import org.xbib.util.graph.persistent.GraphNode;

/**
 * Implementation for {@link GraphNode}
 *
 * @param <V> Type of node labels. Must implement {@link Object#hashCode} and {@link Object#equals}.
 * @param <E> Type of edge labels. Must implement {@link Object#hashCode} and {@link Object#equals}.
 */
public abstract class InternalGraphNode<V, E> implements GraphNode<V, E> {

    private final GraphEdges<E> incoming;
    private final int node;
    private final V nodeLabel;
    private final GraphEdges<E> outgoing;

    public InternalGraphNode(GraphEdges<E> incoming, int node, V nodeLabel, GraphEdges<E> outgoing) {
        this.incoming = incoming;
        this.node = node;
        this.nodeLabel = nodeLabel;
        this.outgoing = outgoing;
    }

    @Override
    public GraphEdges<E> incoming() {
        return incoming;
    }

    @Override
    public int node() {
        return node;
    }

    @Override
    public V label() {
        return nodeLabel;
    }

    @Override
    public GraphEdges<E> outgoing() {
        return outgoing;
    }

    @Override
    public GraphNode<V, E> modifyIncoming(GraphEdges<E> newIncoming) {
        return modify(newIncoming, outgoing);
    }

    @Override
    public GraphNode<V, E> modifyOut(GraphEdges<E> newOut) {
        return modify(incoming, newOut);
    }

    @Override
    public GraphNode<V, E> addSelf(E edgeLabel) {
        return modify(incoming.put(node, edgeLabel), outgoing.put(node, edgeLabel));
    }

    @Override
    public GraphNode<V, E> removeSelf(E edgeLabel) {
        return modify(incoming.remove(node, edgeLabel), outgoing.remove(node, edgeLabel));
    }

    @Override
    public GraphNode<V, E> removeSelfAll() {
        return modify(incoming.removeAll(node), outgoing.removeAll(node));
    }

    @Override
    public abstract GraphNode<V, E> modify(GraphEdges<E> newInc, GraphEdges<E> newOut);

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + incoming.hashCode();
        result = prime * result + node;
        result = prime * result + ((nodeLabel == null) ? 0 : nodeLabel.hashCode());
        result = prime * result + outgoing.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        @SuppressWarnings("unchecked")
        final InternalGraphNode<V, E> other = (InternalGraphNode<V, E>) obj;
        if (!incoming.equals(other.incoming)) {
            return false;
        }
        if (node != other.node) {
            return false;
        }
        if (nodeLabel == null) {
            if (other.nodeLabel != null) {
                return false;
            }
        } else if (!nodeLabel.equals(other.nodeLabel)) {
            return false;
        }
        return outgoing.equals(other.outgoing);
    }

    @Override
    public String toString() {
        return "(" + incoming.toString() + ", " + node + ", " + nodeLabel + ", " + outgoing.toString() + ")";
    }
}

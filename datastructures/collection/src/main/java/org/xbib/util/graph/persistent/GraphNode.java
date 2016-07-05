package org.xbib.util.graph.persistent;

/**
 * Persistent, labeled, graph node data structure.
 *
 * @param <V> Type of node labels. Must implement {@link Object#hashCode} and {@link Object#equals}.
 * @param <E> Type of edge labels. Must implement {@link Object#hashCode} and {@link Object#equals}.
 */
public interface GraphNode<V, E> {
    /**
     * Gets the incoming edges.
     *
     * @return Incoming edges.
     */
    GraphEdges<E> incoming();

    /**
     * Gets the node identifier.
     *
     * @return Node identifier.
     */
    int node();

    /**
     * Gets the node label. Can be null.
     *
     * @return Node label.
     */
    V label();

    /**
     * Gets the outgoing edges.
     *
     * @return Outgoing edges.
     */
    GraphEdges<E> outgoing();

    /**
     * Modifies the incoming and outgoing edges.
     *
     * @param newIncoming New incoming edges.
     * @param newOutgoing New outgoing edges.
     * @return Graph node with new incoming and outgoing edges.
     */
    GraphNode<V, E> modify(GraphEdges<E> newIncoming, GraphEdges<E> newOutgoing);

    /**
     * Modifies the incoming edges.
     *
     * @param newIncoming New incoming edges.
     * @return Graph node with new incoming edges.
     */
    GraphNode<V, E> modifyIncoming(GraphEdges<E> newIncoming);

    /**
     * Modifies the outgoing edges.
     *
     * @param newOutgoing New outgoing edges.
     * @return Graph node with new outgoing edges.
     */
    GraphNode<V, E> modifyOut(GraphEdges<E> newOutgoing);


    /**
     * Adds edge to itself with given label.
     *
     * @param edgeLabel Edge label.
     * @return Graph node with edge added.
     */
    GraphNode<V, E> addSelf(E edgeLabel);

    /**
     * Removes edge to itself with given label.
     *
     * @param edgeLabel Edge label.
     * @return Graph node with edge removed.
     */
    GraphNode<V, E> removeSelf(E edgeLabel);

    /**
     * Removes all edges to itself.
     *
     * @return Graph node with edges removed.
     */
    GraphNode<V, E> removeSelfAll();
}

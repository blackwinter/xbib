package org.xbib.util.graph.persistent;

/**
 * Builder for efficiently creating graphs in a mutable way.
 *
 * @param <V> Type of node labels. Must implement {@link Object#hashCode} and {@link Object#equals}.
 * @param <E> Type of edge labels. Must implement {@link Object#hashCode} and {@link Object#equals}.
 */
public interface GraphBuilder<V, E> {
    /**
     * @see Graph#addNode(int, Object)
     */
    GraphBuilder<V, E> addNode(int node, V nodeLabel) throws IllegalStateException;

    /**
     * @see Graph#addEdge(int, int, Object)
     */
    GraphBuilder<V, E> addEdge(int srcNode, int dstNode, E edgeLabel) throws IllegalStateException;

    /**
     * @see Graph#addSelfEdge(int, Object)
     */
    GraphBuilder<V, E> addSelfEdge(int node, E edgeLabel) throws IllegalStateException;

    /**
     * Builds a persistent graph from the current state of the builder.
     *
     * @return Persistent graph.
     */
    Graph<V, E> build();
}

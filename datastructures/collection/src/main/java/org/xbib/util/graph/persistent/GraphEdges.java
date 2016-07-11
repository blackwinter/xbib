package org.xbib.util.graph.persistent;

/**
 * Persistent, labeled, graph edges collection.
 *
 * @param <E> Type of edge labels. Must implement {@link Object#hashCode} and {@link Object#equals}.
 */
public interface GraphEdges<E> {
    /**
     * Checks if there is an edge for given node.
     *
     * @param node Node identifier.
     * @return True if edge exists, false otherwise.
     */
    boolean containsEdge(int node);

    /**
     * Checks if there is an edge for given node, with given label.
     *
     * @param node  Node identifier.
     * @param label Edge label.
     * @return True if edge exists, false otherwise.
     */
    boolean containsEdge(int node, E label);

    /**
     * Gets all nodes for which there is an edge.
     *
     * @return Node identifiers.
     */
    Iterable<Integer> nodes();

    /**
     * Gets all edge labels for edges for given node.
     *
     * @param node Node identifier.
     * @return Edge labels.
     */
    Iterable<E> labels(int node);

    /**
     * Gets all edges
     *
     * @return the Edges
     */
    Iterable<? extends Iterable<E>> edges();

    /**
     * Adds edge for given node, with given label.
     *
     * @param node  Node identifier.
     * @param label Edge label.
     * @return Edges with edge added.
     */
    GraphEdges<E> put(int node, E label);

    /**
     * Removes edge for given node, with given label.
     *
     * @param node  Node identifier.
     * @param label Edge label.
     * @return Edges with edge removed.
     */
    GraphEdges<E> remove(int node, E label);

    /**
     * Removes all edges for given node.
     *
     * @param node Node identifier.
     * @return Edges with edges removed.
     */
    GraphEdges<E> removeAll(int node);
}

package org.xbib.util.graph.persistent;

/**
 * Persistent, labeled, directed, multigraph. Nodes are identified by integers, edges as incoming and outgoing edges
 * between these integers. Multiple edges between nodes are allowed, but their labels must be distinct. Node labels may
 * be null. Edge labels must not be null.
 *
 * @param <V> Type of node labels. Must implement {@link Object#hashCode} and {@link Object#equals}.
 * @param <E> Type of edge labels. Must implement {@link Object#hashCode} and {@link Object#equals}.
 */
public interface Graph<V, E> {
    /**
     * Checks if the graph is empty; it contains no nodes.
     *
     * @return True if the graph has no nodes, false otherwise.
     */
    boolean isEmpty();

    /**
     * Checks if the graph contains a node with given identifier.
     *
     * @param node Node identifier.
     * @return True if graph contains the node, false otherwise.
     */
    boolean containsNode(int node);

    /**
     * Checks if the graph contains a node with given identifier and node label.
     *
     * @param node      Node identifier
     * @param nodeLabel Node label, can be null.
     * @return True if graph contains given node with label, false otherwise.
     */
    boolean containsNode(int node, V nodeLabel);

    /**
     * Checks if the graph contains an edge from given source node to destination node.
     *
     * @param srcNode Source node identifier.
     * @param dstNode Destination node identifier.
     * @return True if graph contains an edge between given nodes, false otherwise.
     */
    boolean containsEdge(int srcNode, int dstNode);

    /**
     * Checks if the graph contains an edge from given source node to destination node, with a certain label.
     *
     * @param srcNode   Source node identifier.
     * @param dstNode   Destination node identifier.
     * @param edgeLabel Edge label.
     * @return True if graph contains an edge between given nodes with a certain label, false otherwise.
     */
    boolean containsEdge(int srcNode, int dstNode, E edgeLabel);

    /**
     * Gets the graph node with given identifier.
     *
     * @param node Node identifier.
     * @return Graph node, or null if node does not exist.
     */
    GraphNode<V, E> get(int node);

    /**
     * Gets the node identifiers.
     *
     * @return Node identifiers.
     */
    Iterable<Integer> nodeIds();

    /**
     * Gets the graph nodes.
     *
     * @return Graph nodes.
     */
    Iterable<? extends GraphNode<V, E>> nodes();


    /**
     * Adds a node to the graph with given identifier and label.
     *
     * @param node      Node identifier.
     * @param nodeLabel Node label, can be null.
     * @return Graph with node added.
     * @throws IllegalStateException When node with given identifier already exists.
     */
    Graph<V, E> addNode(int node, V nodeLabel) throws IllegalStateException;

    /**
     * Removes the node with given identifier from the graph.
     *
     * @param node Node identifier.
     * @return Graph with node removed.
     * @throws IllegalStateException When node with given identifier does not exist.
     */
    Graph<V, E> removeNode(int node) throws IllegalStateException;

    /**
     * Adds an edge from source to destination node, with given label.
     *
     * @param srcNode   Source node identifier.
     * @param dstNode   Destination node identifier.
     * @param edgeLabel Edge label.
     * @return Graph with edge added.
     * @throws IllegalStateException When source or destination node does not exist.
     * @throws IllegalStateException When there is already an edge between source and destination with given label.
     */
    Graph<V, E> addEdge(int srcNode, int dstNode, E edgeLabel) throws IllegalStateException;

    /**
     * Removes an edge from source to destination node, with given label.
     *
     * @param srcNode   Source node identifier.
     * @param dstNode   Destination node identifier.
     * @param edgeLabel Edge label.
     * @return Graph with edge removed.
     * @throws IllegalStateException When source or destination node does not exist.
     * @throws IllegalStateException When there is no edge between source and destination with given label.
     */
    Graph<V, E> removeEdge(int srcNode, int dstNode, E edgeLabel) throws IllegalStateException;

    /**
     * Removes all edges from source to destination node.
     *
     * @param srcNode Source node identifier.
     * @param dstNode Destination node identifier.
     * @return Graph with edges removed.
     * @throws IllegalStateException When source or destination node does not exist.
     * @throws IllegalStateException When there are no edges between source and destination.
     */
    Graph<V, E> removeAllEdges(int srcNode, int dstNode) throws IllegalStateException;

    /**
     * Adds an edge from given node to itself, with given label.
     *
     * @param node      Node identifier.
     * @param edgeLabel Edge label.
     * @return Graph with edge added.
     * @throws IllegalStateException When node does not exist.
     * @throws IllegalStateException When there is already an edge to itself with given label.
     */
    Graph<V, E> addSelfEdge(int node, E edgeLabel) throws IllegalStateException;

    /**
     * Removes an edge from given node to itself, with given label.
     *
     * @param node      Node identifier.
     * @param edgeLabel Edge label.
     * @return Graph with edge removed.
     * @throws IllegalStateException When node does not exist.
     * @throws IllegalStateException When there is no edge from the node to itself with given label.
     */
    Graph<V, E> removeSelfEdge(int node, E edgeLabel) throws IllegalStateException;

    /**
     * Removes all edges from node to itself.
     *
     * @param node Node identifier.
     * @return Graph with edges removed.
     * @throws IllegalStateException When node does not exist.
     * @throws IllegalStateException When there are no edges from node to itself.
     */
    Graph<V, E> removeAllSelfEdges(int node) throws IllegalStateException;
}

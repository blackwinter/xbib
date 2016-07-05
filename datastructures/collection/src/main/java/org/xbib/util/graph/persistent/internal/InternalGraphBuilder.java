package org.xbib.util.graph.persistent.internal;

import org.xbib.util.graph.persistent.Graph;
import org.xbib.util.graph.persistent.GraphBuilder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Implementation for {@link GraphBuilder} that keeps nodes and edges in a mutable data structure
 *
 * @param <V> Type of node labels. Must implement {@link Object#hashCode} and {@link Object#equals}.
 * @param <E> Type of edge labels. Must implement {@link Object#hashCode} and {@link Object#equals}.
 */
public abstract class InternalGraphBuilder<V, E> implements GraphBuilder<V, E> {
    protected final Map<Integer, Node<V, E>> nodes = new HashMap<>();

    @Override
    public GraphBuilder<V, E> addNode(int node, V nodeLabel) throws IllegalStateException {
        if (nodes.containsKey(node)) {
            throw new IllegalStateException();
        }

        nodes.put(node, new Node<V, E>(node, nodeLabel));
        return this;
    }

    @Override
    public GraphBuilder<V, E> addEdge(int srcNode, int dstNode, E edgeLabel) throws IllegalStateException {
        if (!nodes.containsKey(srcNode)) {
            throw new IllegalStateException();
        }
        if (!nodes.containsKey(dstNode)) {
            throw new IllegalStateException();
        }

        final Node<V, E> srcNodeObj = nodes.get(srcNode);
        final Node<V, E> dstNodeObj = nodes.get(dstNode);

        srcNodeObj.addOutEdge(dstNode, edgeLabel);
        dstNodeObj.addIncEdge(srcNode, edgeLabel);

        return this;
    }

    @Override
    public GraphBuilder<V, E> addSelfEdge(int node, E edgeLabel) throws IllegalStateException {
        if (!nodes.containsKey(node)) {
            throw new IllegalStateException();
        }
        final Node<V, E> nodeObj = nodes.get(node);
        nodeObj.addSelfEdge(edgeLabel);

        return this;
    }

    @Override
    abstract public Graph<V, E> build();

    protected static class Node<V, E> {
        public final Map<Integer, Set<E>> inc = new HashMap<>();
        public final int node;
        public final V label;
        public final Map<Integer, Set<E>> out = new HashMap<>();


        public Node(int node, V label) {
            this.node = node;
            this.label = label;
        }

        public void addIncEdge(int srcNode, E edgeLabel) throws IllegalStateException {
            Set<E> edges = inc.get(srcNode);
            if (edges == null) {
                edges = new HashSet<E>();
                inc.put(srcNode, edges);
            }

            if (edges.contains(edgeLabel)) {
                throw new IllegalStateException();
            }

            edges.add(edgeLabel);
        }

        public void addOutEdge(int dstNode, E edgeLabel) throws IllegalStateException {
            Set<E> edges = out.get(dstNode);
            if (edges == null) {
                edges = new HashSet<E>();
                out.put(dstNode, edges);
            }

            if (edges.contains(edgeLabel)) {
                throw new IllegalStateException();
            }

            edges.add(edgeLabel);
        }

        public void addSelfEdge(E edgeLabel) throws IllegalStateException {
            addIncEdge(node, edgeLabel);
            addOutEdge(node, edgeLabel);
        }
    }
}

package org.xbib.util.graph.persistent.impl;

import org.xbib.util.graph.persistent.Graph;
import org.xbib.util.graph.persistent.GraphBuilder;

public class PersistentHashGraphBuilder<V, E> implements GraphBuilder<V, E> {

    private Graph<V, E> graph;

    public PersistentHashGraphBuilder() {
        this.graph = new PersistentHashGraph<V, E>();
    }

    @Override
    public GraphBuilder<V, E> addNode(int node, V nodeLabel) {
        graph = graph.addNode(node, nodeLabel);
        return this;
    }

    @Override
    public GraphBuilder<V, E> addEdge(int srcNode, int dstNode, E edgeLabel) {
        graph = graph.addEdge(srcNode, dstNode, edgeLabel);
        return this;
    }

    @Override
    public GraphBuilder<V, E> addSelfEdge(int node, E edgeLabel) {
        graph = graph.addSelfEdge(node, edgeLabel);
        return this;
    }

    @Override
    public Graph<V, E> build() {
        return graph;
    }
}

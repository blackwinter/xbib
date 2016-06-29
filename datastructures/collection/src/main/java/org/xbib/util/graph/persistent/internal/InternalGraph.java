package org.xbib.util.graph.persistent.internal;

import org.xbib.util.graph.persistent.Graph;
import org.xbib.util.graph.persistent.GraphNode;
import org.xbib.util.graph.persistent.IntMap;

import java.util.Objects;

/**
 * Implementation for {@link Graph}
 *
 * @param <V> Type of node labels. Must implement {@link Object#hashCode} and {@link Object#equals}.
 * @param <E> Type of edge labels. Must implement {@link Object#hashCode} and {@link Object#equals}.
 */
public abstract class InternalGraph<V, E> implements Graph<V, E> {

    private final IntMap<GraphNode<V, E>> contexts;

    public InternalGraph(IntMap<GraphNode<V, E>> contexts) {
        this.contexts = contexts;
    }

    @Override
    public boolean isEmpty() {
        return contexts.isEmpty();
    }

    @Override
    public boolean containsNode(int node) {
        return contexts.contains(node);
    }

    @Override
    public boolean containsNode(int node, V nodeLabel) {
        final GraphNode<V, E> context = contexts.get(node);
        return context != null && Objects.equals(nodeLabel, context.label());
    }

    @Override
    public boolean containsEdge(int srcNode, int dstNode) {
        final GraphNode<V, E> srcContext = contexts.get(srcNode);
        return srcContext != null && srcContext.outgoing().containsEdge(dstNode);
    }

    @Override
    public boolean containsEdge(int srcNode, int dstNode, E edgeLabel) {
        final GraphNode<V, E> srcContext = contexts.get(srcNode);
        return srcContext != null && srcContext.outgoing().containsEdge(dstNode, edgeLabel);
    }

    @Override
    public GraphNode<V, E> get(int node) {
        return contexts.get(node);
    }

    @Override
    public Iterable<Integer> nodeIds() {
        return contexts.keys();
    }

    @Override
    public Iterable<? extends GraphNode<V, E>> nodes() {
        return contexts.values();
    }


    @Override
    public Graph<V, E> addNode(int node, V nodeLabel) throws IllegalStateException {
        if (contexts.contains(node)) {
            throw new IllegalStateException("Node " + node + " already exists");
        }
        final GraphNode<V, E> context = createNode(node, nodeLabel);
        final IntMap<GraphNode<V, E>> newContexts = contexts.put(node, context);
        return createGraph(newContexts);
    }

    @Override
    public Graph<V, E> removeNode(int node) throws IllegalStateException {
        final GraphNode<V, E> removedContext = contexts.get(node);
        if (removedContext == null) {
            throw new IllegalStateException("Node " + node + " does not exist");
        }

        IntMap<GraphNode<V, E>> newContexts = contexts;
        for (Integer inc : removedContext.incoming().nodes()) {
            GraphNode<V, E> ctx = contexts.get(inc);
            ctx = ctx.modifyOut(ctx.outgoing().removeAll(node));
            newContexts = newContexts.put(inc, ctx);
        }
        for (Integer out : removedContext.outgoing().nodes()) {
            GraphNode<V, E> ctx = newContexts.get(out);
            ctx = ctx.modifyIncoming(ctx.incoming().removeAll(node));
            newContexts = newContexts.put(out, ctx);
        }
        newContexts = newContexts.remove(node);
        return createGraph(newContexts);
    }

    @Override
    public Graph<V, E> addEdge(int srcNode, int dstNode, E edgeLabel) throws IllegalStateException {
        if (srcNode == dstNode) {
            return addSelfEdge(srcNode, edgeLabel);
        }

        if (edgeLabel == null) {
            throw new IllegalStateException("Edge labels may not be null");
        }

        GraphNode<V, E> srcContext = contexts.get(srcNode);
        if (srcContext == null) {
            throw new IllegalStateException("Source node " + srcNode + " does not exist");
        }
        GraphNode<V, E> dstContext = contexts.get(dstNode);
        if (dstContext == null) {
            throw new IllegalStateException("Destination node " + dstNode + " does not exist");
        }
        if (srcContext.outgoing().containsEdge(dstNode, edgeLabel)) {
            throw new IllegalStateException("Source node " + srcNode + " already has an outgoing " + edgeLabel
                    + "-labeled edge to destination node " + dstNode);
        }
        if (dstContext.incoming().containsEdge(srcNode, edgeLabel)) {
            throw new IllegalStateException("Destination node " + dstNode + " already has an incoming " + edgeLabel
                    + "-labeled edge from source node " + srcNode);
        }

        srcContext = srcContext.modifyOut(srcContext.outgoing().put(dstNode, edgeLabel));
        dstContext = dstContext.modifyIncoming(dstContext.incoming().put(srcNode, edgeLabel));
        IntMap<GraphNode<V, E>> newContexts = contexts;
        newContexts = newContexts.put(srcNode, srcContext);
        newContexts = newContexts.put(dstNode, dstContext);
        return createGraph(newContexts);
    }

    @Override
    public Graph<V, E> removeEdge(int srcNode, int dstNode, E edgeLabel) throws IllegalStateException {
        if (srcNode == dstNode) {
            return removeSelfEdge(srcNode, edgeLabel);
        }

        GraphNode<V, E> srcContext = contexts.get(srcNode);
        if (srcContext == null) {
            throw new IllegalStateException("Source node " + srcNode + " does not exist");
        }
        GraphNode<V, E> dstContext = contexts.get(dstNode);
        if (dstContext == null) {
            throw new IllegalStateException("Destination node " + dstNode + " does not exist");
        }
        if (!srcContext.outgoing().containsEdge(dstNode, edgeLabel)) {
            throw new IllegalStateException("Source node " + srcNode + " does not have an " + edgeLabel
                    + "-labeled outgoing edge to destination node " + dstNode);
        }
        if (!dstContext.incoming().containsEdge(srcNode, edgeLabel)) {
            throw new IllegalStateException("Destination node " + dstNode + " does not have an incoming " + edgeLabel
                    + "-labeled edge from source node" + srcNode);
        }
        srcContext = srcContext.modifyOut(srcContext.outgoing().remove(dstNode, edgeLabel));
        dstContext = dstContext.modifyIncoming(dstContext.incoming().remove(srcNode, edgeLabel));
        IntMap<GraphNode<V, E>> newContexts = contexts;
        newContexts = newContexts.put(srcNode, srcContext);
        newContexts = newContexts.put(dstNode, dstContext);
        return createGraph(newContexts);
    }

    @Override
    public Graph<V, E> removeAllEdges(int srcNode, int dstNode) throws IllegalStateException {
        if (srcNode == dstNode) {
            return removeAllSelfEdges(srcNode);
        }
        GraphNode<V, E> srcContext = contexts.get(srcNode);
        if (srcContext == null) {
            throw new IllegalStateException("Source node " + srcNode + " does not exist");
        }
        GraphNode<V, E> dstContext = contexts.get(dstNode);
        if (dstContext == null) {
            throw new IllegalStateException("Destination node " + dstNode + " does not exist");
        }
        if (!srcContext.outgoing().containsEdge(dstNode)) {
            throw new IllegalStateException("Source node " + srcNode
                    + " does not have any outgoing edges to destination node " + dstNode);
        }
        if (!dstContext.incoming().containsEdge(srcNode)) {
            throw new IllegalStateException("Destination node " + dstNode
                    + " does not have any incoming edges from source node" + srcNode);
        }
        srcContext = srcContext.modifyOut(srcContext.outgoing().removeAll(dstNode));
        dstContext = dstContext.modifyIncoming(dstContext.incoming().removeAll(srcNode));
        IntMap<GraphNode<V, E>> newContexts = contexts;
        newContexts = newContexts.put(srcNode, srcContext);
        newContexts = newContexts.put(dstNode, dstContext);
        return createGraph(newContexts);
    }

    @Override
    public Graph<V, E> addSelfEdge(int node, E edgeLabel) throws IllegalStateException {
        if (edgeLabel == null) {
            throw new IllegalStateException("Edge labels may not be null");
        }
        GraphNode<V, E> context = contexts.get(node);
        if (context == null) {
            throw new IllegalStateException("Node " + node + " does not exist");
        }
        if (context.incoming().containsEdge(node, edgeLabel)) {
            throw new IllegalStateException("Node " + node + " already has an incoming " + edgeLabel
                    + "-labeled edge to itself");
        }
        if (context.outgoing().containsEdge(node, edgeLabel)) {
            throw new IllegalStateException("Node " + node + " already has an outgoing " + edgeLabel
                    + "-labeled edge to itself");
        }
        context = context.addSelf(edgeLabel);
        final IntMap<GraphNode<V, E>> newContexts = contexts.put(node, context);
        return createGraph(newContexts);
    }

    @Override
    public Graph<V, E> removeSelfEdge(int node, E edgeLabel) throws IllegalStateException {
        GraphNode<V, E> context = contexts.get(node);
        if (context == null) {
            throw new IllegalStateException("Node " + node + " does not exist");
        }
        if (!context.incoming().containsEdge(node, edgeLabel)) {
            throw new IllegalStateException("Node " + node + " does not have an incoming " + edgeLabel
                    + "-labeled edge to itself");
        }
        if (!context.outgoing().containsEdge(node, edgeLabel)) {
            throw new IllegalStateException("Node " + node + " does not have an outgoing " + edgeLabel
                    + "-labeled edge to itself");
        }
        context = context.removeSelf(edgeLabel);
        final IntMap<GraphNode<V, E>> newContexts = contexts.put(node, context);
        return createGraph(newContexts);
    }

    @Override
    public Graph<V, E> removeAllSelfEdges(int node) throws IllegalStateException {
        GraphNode<V, E> context = contexts.get(node);
        if (context == null) {
            throw new IllegalStateException("Node " + node + " does not exist");
        }
        if (!context.incoming().containsEdge(node)) {
            throw new IllegalStateException("Node " + node + " does not have any incoming edges to itself");
        }
        if (!context.outgoing().containsEdge(node)) {
            throw new IllegalStateException("Node " + node + " does not have any outgoing edges to itself");
        }
        context = context.removeSelfAll();
        final IntMap<GraphNode<V, E>> newContexts = contexts.put(node, context);
        return createGraph(newContexts);
    }

    public abstract Graph<V, E> createGraph(IntMap<GraphNode<V, E>> contexts);

    public abstract GraphNode<V, E> createNode(int node, V nodeLabel);

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + contexts.hashCode();
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
        final InternalGraph<V, E> other = (InternalGraph<V, E>) obj;
        return contexts.equals(other.contexts);
    }

    @Override
    public String toString() {
        return contexts.toString();
    }
}

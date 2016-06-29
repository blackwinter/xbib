package org.xbib.util.graph.persistent.internal;

import org.xbib.util.graph.persistent.GraphEdges;
import org.xbib.util.graph.persistent.IntMap;
import org.xbib.util.graph.persistent.ObjectSet;

import java.util.Collection;
import java.util.Collections;

/**
 * Implementation for {@link GraphEdges}
 *
 * @param <E> Type of edge labels. Must implement {@link Object#hashCode} and {@link Object#equals}.
 */
public abstract class InternalGraphEdges<E> implements GraphEdges<E> {

    private final IntMap<ObjectSet<E>> edges;

    public InternalGraphEdges(IntMap<ObjectSet<E>> edges) {
        this.edges = edges;
    }

    @Override
    public boolean containsEdge(int node) {
        return edges.contains(node);
    }

    @Override
    public boolean containsEdge(int node, E edgeLabel) {
        final ObjectSet<E> edgeLabels = edges.get(node);
        return edgeLabels != null && edgeLabels.contains(edgeLabel);
    }

    @Override
    public Iterable<Integer> nodes() {
        return edges.keys();
    }

    @Override
    public Iterable<E> labels(int node) {
        final ObjectSet<E> edgeLabels = edges.get(node);
        if (edgeLabels == null) {
            return Collections.emptyList();
        }
        return edgeLabels;
    }

    @Override
    public Iterable<? extends Iterable<E>> edges() {
        return edges.values();
    }

    @Override
    public GraphEdges<E> put(int node, E label) {
        final IntMap<ObjectSet<E>> newEdges;
        final ObjectSet<E> labels = edges.get(node);
        if (labels == null) {
            newEdges = edges.put(node, createSet(label));
        } else {
            newEdges = edges.put(node, labels.add(label));
        }
        return createEdges(newEdges);
    }

    @Override
    public GraphEdges<E> remove(int node, E label) {
        final ObjectSet<E> labels = edges.get(node);
        if (labels == null) {
            return this;
        } else {
            final ObjectSet<E> newLabels = labels.remove(label);
            if (newLabels.isEmpty()) {
                return createEdges(edges.remove(node));
            } else {
                return createEdges(edges.put(node, newLabels));
            }
        }
    }

    @Override
    public GraphEdges<E> removeAll(int node) {
        return createEdges(edges.remove(node));
    }

    public abstract GraphEdges<E> createEdges(IntMap<ObjectSet<E>> newEdges);

    public abstract ObjectSet<E> createSet(E label);

    @Override
    public int hashCode() {
        return edges.hashCode();
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
        final InternalGraphEdges<E> other = (InternalGraphEdges<E>) obj;
        return edges.equals(other.edges);
    }

    @Override
    public String toString() {
        return edges.toString();
    }
}

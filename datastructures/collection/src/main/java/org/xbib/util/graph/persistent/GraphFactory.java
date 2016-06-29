package org.xbib.util.graph.persistent;

/**
 * Factory for creating {@link Graph}s and {@link GraphBuilder}s.
 *
 * @param <V> Type of node labels. Must implement {@link Object#hashCode} and {@link Object#equals}.
 * @param <E> Type of edge labels. Must implement {@link Object#hashCode} and {@link Object#equals}.
 */
public interface GraphFactory<V, E> {
    Graph<V, E> of();

    GraphBuilder<V, E> builder();
}

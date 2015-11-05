package org.xbib.query;

/**
 * Query facet
 */
public interface QueryFacet<V> extends QueryOption<V> {
    /**
     * The size of the facet
     *
     * @return the facet size
     */
    int getSize();

    /**
     * Get the filter name which must be used for filtering facet entries
     *
     * @return the filter name
     */
    String getFilterName();

}

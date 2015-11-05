package org.xbib.query.cql;

/**
 * Sorted query
 */
public class SortedQuery extends AbstractNode {

    private Query query;

    private SortSpec spec;

    SortedQuery(Query query, SortSpec spec) {
        this.query = query;
        this.spec = spec;
    }

    SortedQuery(Query query) {
        this.query = query;
    }

    public Query getQuery() {
        return query;
    }

    public SortSpec getSortSpec() {
        return spec;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return query != null && spec != null ? query + " sortby " + spec
                : query != null ? query.toString() : "";
    }
}
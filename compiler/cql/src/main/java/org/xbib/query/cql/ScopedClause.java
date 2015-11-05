package org.xbib.query.cql;

/**
 * Scoped clause. This is a recursive data structure with a SearchClause and
 * optionally a ScopedClause.
 * SearchClause and ScopedClause are connected through a BooleanGroup.
 */
public class ScopedClause extends AbstractNode {

    private ScopedClause clause;
    private BooleanGroup booleangroup;
    private SearchClause search;

    ScopedClause(ScopedClause clause, BooleanGroup bg, SearchClause search) {
        this.clause = clause;
        this.booleangroup = bg;
        this.search = search;
    }

    ScopedClause(SearchClause search) {
        this.search = search;
    }

    public ScopedClause getScopedClause() {
        return clause;
    }

    public BooleanGroup getBooleanGroup() {
        return booleangroup;
    }

    public SearchClause getSearchClause() {
        return search;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        String s = search.toString();
        boolean hasQuery = s.length() > 0;
        return clause != null && hasQuery ? clause + " " + booleangroup + " " + search
                : clause != null ? clause.toString()
                : hasQuery ? search.toString()
                : "";
    }

}

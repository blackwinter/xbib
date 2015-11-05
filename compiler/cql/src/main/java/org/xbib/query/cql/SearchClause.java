package org.xbib.query.cql;

import org.xbib.query.cql.model.CQLQueryModel;

/**
 * Search clause
 */
public class SearchClause extends AbstractNode {

    private Query query;
    private Index index;
    private Relation relation;
    private Term term;

    SearchClause(Query query) {
        this.query = query;
    }

    SearchClause(Index index, Relation relation, Term term) {
        this.index = index;
        this.relation = relation;
        this.term = term;
    }

    SearchClause(Term term) {
        this.term = term;
    }

    public Query getQuery() {
        return query;
    }

    public Index getIndex() {
        return index;
    }

    public Term getTerm() {
        return term;
    }

    public Relation getRelation() {
        return relation;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    /**
     * @return CQL string
     */
    @Override
    public String toString() {
        return query != null && query.toString().length() > 0 ? "(" + query + ")"
                : query != null ? ""
                : index != null && !CQLQueryModel.isVisible(index.getContext()) ? ""
                : index != null ? index + " " + relation + " " + term
                : term != null ? term.toString()
                : null;
    }

}

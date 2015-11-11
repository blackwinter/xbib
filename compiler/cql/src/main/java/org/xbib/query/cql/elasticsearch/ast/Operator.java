package org.xbib.query.cql.elasticsearch.ast;

import org.xbib.query.cql.elasticsearch.Visitor;

/**
 * Elasticsearch operators
 */
public enum Operator implements Node {
    EQUALS(2),
    NOT_EQUALS(2),
    RANGE_LESS_THAN(2),
    RANGE_LESS_OR_EQUALS(2),
    RANGE_GREATER_THAN(2),
    RANGE_GREATER_OR_EQUAL(2),
    RANGE_WITHIN(2),
    AND(2),
    ANDNOT(2),
    OR(2),
    PROX(2),
    ALL(2),
    ANY(2),
    PHRASE(2),
    TERM_FILTER(2),
    QUERY_FILTER(2),
    SORT(0),
    TERMS_FACET(0),
    OR_FILTER(2),
    AND_FILTER(2),
    MATCH_ALL(0);


    private final int arity;

    Operator(int arity) {
        this.arity = arity;
    }

    @Override
    public boolean isVisible() {
        return true;
    }

    @Override
    public TokenType getType() {
        return TokenType.OPERATOR;
    }

    public int getArity() {
        return arity;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return this.name();
    }

}

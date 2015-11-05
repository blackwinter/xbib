package org.xbib.query.cql.elasticsearch.ast;

import org.xbib.query.cql.elasticsearch.Visitor;

/**
 * This is a modifier node for Elasticsearch query language
 */
public class Modifier implements Node {

    private Node name;
    private Node term;

    public Modifier(Node name, Node term) {
        this.name = name;
        this.term = term;
    }

    public Modifier(Node name) {
        this.name = name;
    }

    public Node getName() {
        return name;
    }

    public Node getTerm() {
        return term;
    }

    @Override
    public boolean isVisible() {
        return true;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public TokenType getType() {
        return TokenType.OPERATOR;
    }

    @Override
    public String toString() {
        return name + "=" + term;
    }
}

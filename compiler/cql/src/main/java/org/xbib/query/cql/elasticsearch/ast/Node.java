package org.xbib.query.cql.elasticsearch.ast;

import org.xbib.query.cql.elasticsearch.Visitor;

/**
 * This node class is the base class for the Elasticsearch Query Lange abstract syntax tree
 */
public interface Node {

    void accept(Visitor visitor);

    boolean isVisible();

    TokenType getType();

}
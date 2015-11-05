package org.xbib.query.cql;

/**
 * This is a node interface for the CQL abstract syntax tree
 */
public interface Node extends Comparable<Node> {

    /**
     * Accept a visitor on this node
     *
     * @param visitor the visitor
     */
    void accept(Visitor visitor);
}

package org.xbib.query.cql;

/**
 * This abstract node class is the base class for the CQL abstract syntax tree
 */
public abstract class AbstractNode implements Node {

    /**
     * Try to accept this node by a visitor
     *
     * @param visitor the visitor
     */
    @Override
    public abstract void accept(Visitor visitor);

    /**
     * Compare this node to another node
     */
    @Override
    public int compareTo(Node object) {
        if (this == object) {
            return 0;
        }
        return object != null ? toString().compareTo(object.toString()) : 1;
    }
}

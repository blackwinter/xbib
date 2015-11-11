package org.xbib.query.cql;

/**
 * A SimpleName consists of a String which is not surrounded by double quotes.
 */
public class SimpleName extends AbstractNode {

    private String name;

    public SimpleName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return name;
    }

}

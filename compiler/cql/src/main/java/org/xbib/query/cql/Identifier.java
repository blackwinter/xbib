package org.xbib.query.cql;

/**
 * An Identifier is a SimpleName or a String in double quotes.
 */
public class Identifier extends AbstractNode {

    private String value;
    private boolean quoted;

    public Identifier(String value) {
        this.value = value;
        this.quoted = true;
    }

    public Identifier(SimpleName name) {
        this.value = name.getName();
        this.quoted = false;
    }

    public String getValue() {
        return value;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return value != null && quoted ? "\"" + value.replaceAll("\"", "\\\\\"") + "\"" : value;
    }
}

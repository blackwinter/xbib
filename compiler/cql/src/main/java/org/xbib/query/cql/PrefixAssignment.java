package org.xbib.query.cql;

/**
 * Prefix assignment
 */
public class PrefixAssignment extends AbstractNode {

    private Term prefix;

    private Term uri;

    public PrefixAssignment(Term prefix, Term uri) {
        this.prefix = prefix;
        this.uri = uri;
    }

    public PrefixAssignment(Term uri) {
        this.uri = uri;
    }

    public Term getPrefix() {
        return prefix;
    }

    public Term getURI() {
        return uri;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "> " + prefix + " = " + uri;
    }

}

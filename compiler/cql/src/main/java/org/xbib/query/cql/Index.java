package org.xbib.query.cql;

/**
 * Abstract syntax tree of CQL - Index
 * The Index consists of <b>context<b> and <b>name<b>
 * The default context is "cql" and is of the same concept like a namespace.
 */
public class Index extends AbstractNode {

    private String context;
    private String name;

    public Index(String name) {
        this.name = name;
        int pos = name.indexOf('.');
        if (pos > 0) {
            this.context = name.substring(0, pos);
            this.name = name.substring(pos + 1);
        }
    }

    public Index(SimpleName name) {
        this(name.getName());
    }

    /**
     * @return the context of the index
     */
    public String getContext() {
        return context;
    }

    /**
     * Get the name of the index
     *
     * @return the name of the index
     */
    public String getName() {
        return name;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return context != null ? context + "." + name : name;
    }

}

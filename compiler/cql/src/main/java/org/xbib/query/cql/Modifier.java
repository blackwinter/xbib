package org.xbib.query.cql;

/**
 * Modifier
 */
public class Modifier extends AbstractNode {

    private SimpleName name;

    private Comparitor op;

    private Term term;

    public Modifier(SimpleName name, Comparitor op, Term term) {
        this.name = name;
        this.op = op;
        this.term = term;
    }

    public Modifier(SimpleName name) {
        this.name = name;
    }

    public SimpleName getName() {
        return name;
    }

    public Comparitor getOperator() {
        return op;
    }

    public Term getTerm() {
        return term;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "/" + (term != null ? name.toString() + op + term : name.toString());
    }

}

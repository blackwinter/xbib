package org.xbib.query.cql;

/**
 * Abstract syntax tree of CQL - Boolean Group
 */
public class BooleanGroup extends AbstractNode {

    private BooleanOperator op;
    private ModifierList modifiers;

    BooleanGroup(BooleanOperator op, ModifierList modifiers) {
        this.op = op;
        this.modifiers = modifiers;
    }

    BooleanGroup(BooleanOperator op) {
        this.op = op;
    }

    public BooleanOperator getOperator() {
        return op;
    }

    public ModifierList getModifierList() {
        return modifiers;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return op != null && modifiers != null ? op + modifiers.toString()
                : op != null ? op.toString() : null;
    }
}

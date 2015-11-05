package org.xbib.query.cql;

/**
 * Relation to a ModifierList.
 */
public class Relation extends AbstractNode {

    private Comparitor comparitor;
    private ModifierList modifiers;

    public Relation(Comparitor comparitor, ModifierList modifiers) {
        this.comparitor = comparitor;
        this.modifiers = modifiers;
    }

    public Relation(Comparitor comparitor) {
        this.comparitor = comparitor;
    }

    public Comparitor getComparitor() {
        return comparitor;
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
        return modifiers != null ? comparitor + modifiers.toString()
                : comparitor.toString();
    }

}

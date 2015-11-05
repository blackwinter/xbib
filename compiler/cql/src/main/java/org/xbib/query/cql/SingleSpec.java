package org.xbib.query.cql;

/**
 * Single spec
 */
public class SingleSpec extends AbstractNode {

    private Index index;
    private ModifierList modifiers;

    public SingleSpec(Index index, ModifierList modifiers) {
        this.index = index;
        this.modifiers = modifiers;
    }

    public SingleSpec(Index index) {
        this.index = index;
    }

    public Index getIndex() {
        return index;
    }

    public ModifierList getModifierList() {
        return modifiers;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return index + (modifiers != null ? modifiers.toString() : "");
    }

}

package org.xbib.query.cql;

import java.util.LinkedList;
import java.util.List;

/**
 * Modifier list. This is a recursive data structure with a Modifier and optionally a ModifierList.
 */
public class ModifierList extends AbstractNode {

    private List<Modifier> modifierList = new LinkedList<Modifier>();

    public ModifierList(ModifierList modifiers, Modifier modifier) {
        modifierList.addAll(modifiers.modifierList);
        modifierList.add(modifier);
    }

    public ModifierList(Modifier modifier) {
        modifierList.add(modifier);
    }

    public List<Modifier> getModifierList() {
        return modifierList;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Modifier m : modifierList) {
            sb.append(m.toString());
        }
        return sb.toString();
    }

}

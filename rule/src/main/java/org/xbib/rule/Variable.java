package org.xbib.rule;

public class Variable implements Expression {
    private String name;

    public Variable(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public boolean interpret(Binding binding) {
        return true;
    }
}
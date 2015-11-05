package org.xbib.query.cql.elasticsearch.ast;

import org.xbib.query.cql.elasticsearch.Visitor;

/**
 * A name for Elasticsearch fields
 */
public class Name implements Node {

    private String name;

    private TokenType type;

    private boolean visible;

    public Name(String name) {
        this(name, true);
    }

    public Name(String name, boolean visible) {
        this.name = name;
        this.visible = visible;
    }

    public String getName() {
        return name;
    }

    public void setType(TokenType type) {
        this.type = type;
    }

    @Override
    public TokenType getType() {
        return type;
    }

    @Override
    public boolean isVisible() {
        return visible;
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

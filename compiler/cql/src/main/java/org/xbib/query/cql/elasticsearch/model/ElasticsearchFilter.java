package org.xbib.query.cql.elasticsearch.model;

import org.xbib.query.QueryFilter;
import org.xbib.query.cql.elasticsearch.ast.Operator;

public class ElasticsearchFilter<V> implements QueryFilter<V>, Comparable<ElasticsearchFilter<V>> {

    private String name;

    private V value;

    private Operator op;

    public ElasticsearchFilter(String name, V value, Operator op) {
        this.name = name;
        this.op = op;
        this.value = value;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setValue(V value) {
        this.value = value;
    }

    public V getValue() {
        return value;
    }

    public Operator getFilterOperation() {
        return op;
    }

    @Override
    public int compareTo(ElasticsearchFilter<V> o) {
        return toString().compareTo(o.toString());
    }

    @Override
    public String toString() {
        return name + " " + op + " " + value;
    }
}

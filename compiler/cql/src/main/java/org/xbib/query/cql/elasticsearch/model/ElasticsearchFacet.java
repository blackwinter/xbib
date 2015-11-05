package org.xbib.query.cql.elasticsearch.model;

import org.xbib.query.QueryFacet;

public final class ElasticsearchFacet<V> implements QueryFacet<V>, Comparable<ElasticsearchFacet<V>> {

    public enum Type {
        TERMS,
        RANGE,
        HISTOGRAM,
        DATEHISTOGRAM,
        FILTER,
        QUERY,
        STATISTICAL,
        TERMS_STATS,
        GEO_DISTANCE
    }

    public static int DEFAULT_FACET_SIZE = 10;

    private Type type;

    private String name;

    private V value;

    private int size;

    public ElasticsearchFacet(Type type, String name, V value) {
        this(type, name, value, DEFAULT_FACET_SIZE);
    }

    public ElasticsearchFacet(Type type, String name, V value, int size) {
        this.type = type;
        this.name = name;
        this.value = value;
        this.size = size;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    @Override
    public void setValue(V value) {
        this.value = value;
    }

    @Override
    public V getValue() {
        return value;
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public String getFilterName() {
        return name;
    }

    @Override
    public int compareTo(ElasticsearchFacet<V> o) {
        return name.compareTo(((ElasticsearchFacet) o).getName());
    }

    @Override
    public String toString() {
        return "facet [name=" + name + ",value=" + value + ",size=" + size + "]";
    }

}

package org.xbib.query.cql.elasticsearch.model;

import org.xbib.query.cql.elasticsearch.ast.Expression;
import org.xbib.query.cql.elasticsearch.ast.Name;
import org.xbib.query.cql.elasticsearch.ast.Node;
import org.xbib.query.cql.elasticsearch.ast.Operator;
import org.xbib.query.cql.elasticsearch.ast.Token;
import org.xbib.query.cql.elasticsearch.ast.TokenType;
import org.xbib.query.cql.model.CQLQueryModel;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * Elasticsearch query model
 */
public final class ElasticsearchQueryModel {

    private final Map<String, Expression> conjunctivefilters;

    private final Map<String, Expression> disjunctivefilters;

    private final Map<String, Expression> facets;

    private Expression sortexpr;

    public ElasticsearchQueryModel() {
        this.conjunctivefilters = new HashMap<String, Expression>();
        this.disjunctivefilters = new HashMap<String, Expression>();
        this.facets = new HashMap<String, Expression>();
    }

    /**
     * Determine if the key has a type. Default type is string.
     *
     * @param key the key to check
     * @return the type of the key
     */
    public TokenType getElasticsearchType(String key) {
        if ("datetime".equals(key)) {
            return TokenType.DATETIME;
        }
        if ("int".equals(key)) {
            return TokenType.INT;
        }
        if ("long".equals(key)) {
            return TokenType.INT;
        }
        if ("float".equals(key)) {
            return TokenType.FLOAT;
        }
        return TokenType.STRING;
    }

    /**
     * Get expression visibility of a given context
     *
     * @param context the context
     * @return true if visible
     */
    public boolean getVisibility(String context) {
        return !CQLQueryModel.isFacetContext(context)
                && !CQLQueryModel.isFilterContext(context)
                && !CQLQueryModel.isOptionContext(context);
    }

    /**
     * Check if this context is the facet context
     *
     * @param context the context
     * @return true if facet context
     */
    public boolean isFacetContext(String context) {
        return CQLQueryModel.isFacetContext(context);
    }

    /**
     * Check if this context is the filter context
     *
     * @param context the context
     * @return true if filter context
     */
    public boolean isFilterContext(String context) {
        return CQLQueryModel.isFilterContext(context);
    }


    public boolean hasFacets() {
        return !facets.isEmpty();
    }

    public void addFacet(String key, String value) {
        ElasticsearchFacet<Node> facet = new ElasticsearchFacet<Node>(ElasticsearchFacet.Type.TERMS, key, new Name(value));
        facets.put(facet.getName(), new Expression(Operator.TERMS_FACET, facet.getValue()));
    }

    public Expression getFacetExpression() {
        return new Expression(Operator.TERMS_FACET, facets.values().toArray(new Node[facets.size()]));
    }

    public void addConjunctiveFilter(String name, Node value, Operator op) {
        addFilter(conjunctivefilters, new ElasticsearchFilter<>(name, value, op));
    }

    public void addDisjunctiveFilter(String name, Node value, Operator op) {
        addFilter(disjunctivefilters, new ElasticsearchFilter<>(name, value, op));
    }

    public boolean hasFilter() {
        return !conjunctivefilters.isEmpty() || !disjunctivefilters.isEmpty();
    }

    /**
     * Get filter expression.
     * Only one filter expression is allowed per query.
     * First, build conjunctive and disjunctive filter terms.
     * If both are null, there is no filter at all.
     * Otherwise, combine conjunctive and disjunctive filter terms with a
     * disjunction, and apply filter function, and return this expression.
     *
     * @return a single filter expression or null if there are no filter terms
     */
    public Expression getFilterExpression() {
        if (!hasFilter()) {
            return null;
        }
        Expression conjunctiveclause = null;
        if (!conjunctivefilters.isEmpty()) {
            conjunctiveclause = new Expression(Operator.AND,
                    conjunctivefilters.values().toArray(new Node[conjunctivefilters.size()]));
        }
        Expression disjunctiveclause = null;
        if (!disjunctivefilters.isEmpty()) {
            disjunctiveclause = new Expression(Operator.OR,
                    disjunctivefilters.values().toArray(new Node[disjunctivefilters.size()]));
        }
        if (conjunctiveclause != null && disjunctiveclause == null) {
            return conjunctiveclause;
        } else if (conjunctiveclause == null && disjunctiveclause != null) {
            return disjunctiveclause;
        } else {
            return new Expression(Operator.OR, conjunctiveclause, disjunctiveclause);
        }
    }

    /**
     * Add sort expression
     *
     * @param indexAndModifier the index with modifiers
     */
    public void setSort(Stack<Node> indexAndModifier) {
        this.sortexpr = new Expression(Operator.SORT, reverse(indexAndModifier).toArray(new Node[indexAndModifier.size()]));
    }

    /**
     * Get sort expression
     *
     * @return the sort expression
     */
    public Expression getSort() {
        return sortexpr;
    }

    /**
     * Helper method to add a filter
     *
     * @param filters the filter list
     * @param filter  the filter to add
     */
    private void addFilter(Map<String, Expression> filters, ElasticsearchFilter<Node> filter) {
        Name name = new Name(filter.getName());
        name.setType(getElasticsearchType(filter.getName()));
        Node value = filter.getValue();
        if (value instanceof Token) {
            value = new Expression(filter.getFilterOperation(), name, value);
        }
        if (filters.containsKey(filter.getName())) {
            Expression expression = filters.get(filter.getName());
            expression = new Expression(expression, value);
            filters.put(filter.getName(), expression);
        } else {
            filters.put(filter.getName(), (Expression) value);
        }
    }

    /**
     * Helper method to reverse an expression stack
     *
     * @param in the stack to reverse
     * @return the reversed stack
     */
    private Stack<Node> reverse(Stack<Node> in) {
        Stack<Node> out = new Stack<Node>();
        while (!in.empty()) {
            out.push(in.pop());
        }
        return out;
    }
}

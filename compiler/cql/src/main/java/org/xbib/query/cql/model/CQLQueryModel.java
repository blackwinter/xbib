package org.xbib.query.cql.model;

import org.xbib.query.cql.AbstractNode;
import org.xbib.query.cql.BooleanOperator;
import org.xbib.query.cql.Term;
import org.xbib.query.cql.model.breadcrumb.FacetBreadcrumbTrail;
import org.xbib.query.cql.model.breadcrumb.FilterBreadcrumbTrail;
import org.xbib.query.cql.model.breadcrumb.OptionBreadcrumbTrail;

/**
 * A CQL query model.
 * <p/>
 * Special contexts are <code>facet</code>, <code>filter</code>,
 * and <code>option</code>.
 * <p/>
 * These contexts form breadcrumb trails.
 * <p/>
 * Bread crumbs provide a means for a server to track an chronologically
 * ordered set of client actions. Bread crumbs are typically rendered as a
 * user-driven constructed list of links, and are useful when
 * users select them to drill down and up in a structure,
 * so that they can find their way and have a notion of where they
 * currently are.
 * <p/>
 * Bread crumbs in the original sense just represent where users are
 * situated in a site hierarchy. For example, when browsing a
 * library catalog, bread crumbs could look like this:
 * <p/>
 * <pre>
 *  Home > Scientific literature > Arts & Human > Philosophy
 * </pre>
 * <p/>
 * or
 * <p/>
 * <pre>
 *   Main library > Branch library > First floor > Rare book room
 * </pre>
 * <p/>
 * These items would be rendered as links to the corresponding location.
 * Classes that implement this interface are responsible for managing
 * such a bread crumb structure. A typical implementation regards
 * bread crumbs as a set of elements.
 * <p/>
 * When a bread crumb is activated that was not in the set yet,
 * it would add it to the set, or when a bread crumb is activated
 * that is already on the set, it would roll back to the corresponding depth.
 * <p/>
 * In this model, multiple bread crumb trails may exist side by side. They are
 * separate and do not depend on each other. There is a list of bread crumb
 * trails, and the notion of a currently active bread crumb within a trail.
 * <p/>
 * This model does not make any presumptions on how it should interact with
 * breadcrumbs except that a breadcrumb model should be serializable into
 * a writer.
 */
public final class CQLQueryModel {

    /**
     * Contexts 'facet', 'filter', and 'option'
     */
    public static final String FACET_INDEX_NAME = "facet";

    public static final String FILTER_INDEX_NAME = "filter";

    public static final String OPTION_INDEX_NAME = "option";

    private static final String AND_OP = " and ";

    private static final String OR_OP = " or ";

    /**
     * the CQL query string
     */
    private String query;

    /**
     * breadcrumb trail for facets
     */
    private FacetBreadcrumbTrail facetTrail;

    /**
     * breadcrumb trail for conjunctive filters
     */
    private FilterBreadcrumbTrail conjunctivefilterTrail;

    /**
     * breadcrumb trail for disjunctive filters
     */
    private FilterBreadcrumbTrail disjunctivefilterTrail;

    /**
     * breadcrumb trail for options
     */
    private OptionBreadcrumbTrail optionTrail;

    public CQLQueryModel() {
        this.facetTrail = new FacetBreadcrumbTrail();
        this.conjunctivefilterTrail = new FilterBreadcrumbTrail(BooleanOperator.AND);
        this.disjunctivefilterTrail = new FilterBreadcrumbTrail(BooleanOperator.OR);
        this.optionTrail = new OptionBreadcrumbTrail();
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getQuery() {
        return query;
    }

    public void addFacet(Facet<Term> facet) {
        facetTrail.add(facet);
    }

    public void removeFacet(Facet<Term> facet) {
        facetTrail.remove(facet);
    }

    /**
     * Add CQL filter
     *
     * @param op     boolean operator, AND for conjunctive filter, OR for disjunctive filter
     * @param filter the filter to add
     */
    public void addFilter(BooleanOperator op, Filter<AbstractNode> filter) {
        if (op == BooleanOperator.AND && !disjunctivefilterTrail.contains(filter)) {
            conjunctivefilterTrail.add(filter);
        }
        if (op == BooleanOperator.OR && !conjunctivefilterTrail.contains(filter)) {
            disjunctivefilterTrail.add(filter);
        }
    }

    /**
     * Remove CQL filter
     *
     * @param filter the filter to remove
     */
    public void removeFilter(Filter<AbstractNode> filter) {
        conjunctivefilterTrail.remove(filter);
        disjunctivefilterTrail.remove(filter);
    }

    public void addOption(Option option) {
        optionTrail.add(option);
    }

    public void removeOption(Option option) {
        optionTrail.remove(option);
    }

    public FacetBreadcrumbTrail getFacetTrail() {
        return facetTrail;
    }

    public String getFilterTrail() {
        StringBuilder sb = new StringBuilder();
        if (!conjunctivefilterTrail.isEmpty()) {
            sb.append(AND_OP).append(conjunctivefilterTrail.toString());
        }
        if (disjunctivefilterTrail.size() == 1) {
            sb.append(OR_OP).append(disjunctivefilterTrail.toString());
        } else if (disjunctivefilterTrail.size() > 1) {
            sb.append(AND_OP).append(disjunctivefilterTrail.toString());
        }
        return sb.toString();
    }

    /**
     * Get the option breadcrumb trail
     *
     * @return the option breadcrumb trail
     */
    public OptionBreadcrumbTrail getOptionTrail() {
        return optionTrail;
    }

    /**
     * Get query  of a given context
     *
     * @param context the context
     * @return true if visible, false if not
     */
    public static boolean isVisible(String context) {
        return !isFacetContext(context)
                && !isFilterContext(context)
                && !isOptionContext(context);
    }

    /**
     * Check if this context is the facet context
     *
     * @param context the context
     * @return true if facet contet
     */
    public static boolean isFacetContext(String context) {
        return FACET_INDEX_NAME.equals(context);
    }

    /**
     * Check if this context is the filter context
     *
     * @param context the context
     * @return true if filter context
     */
    public static boolean isFilterContext(String context) {
        return FILTER_INDEX_NAME.equals(context);
    }

    /**
     * Check if this context is the option context
     *
     * @param context the context
     * @return true if option context
     */
    public static boolean isOptionContext(String context) {
        return OPTION_INDEX_NAME.equals(context);
    }

    /**
     * Write the CQL query model as CQL string
     *
     * @return the query model as CQL
     */
    public String toCQL() {
        StringBuilder sb = new StringBuilder(query);
        String facets = getFacetTrail().toCQL();
        if (facets.length() > 0) {
            sb.append(AND_OP).append(facets);
        }
        String filters = getFilterTrail();
        if (filters.length() > 0) {
            sb.append(filters);
        }
        String options = getOptionTrail().toCQL();
        if (options.length() > 0) {
            sb.append(AND_OP).append(options);
        }
        return sb.toString();
    }
}

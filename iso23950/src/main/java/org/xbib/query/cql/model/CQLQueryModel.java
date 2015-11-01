/*
 * Licensed to Jörg Prante and xbib under one or more contributor
 * license agreements. See the NOTICE.txt file distributed with this work
 * for additional information regarding copyright ownership.
 *
 * Copyright (C) 2012 Jörg Prante and xbib
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses
 * or write to the Free Software Foundation, Inc., 51 Franklin Street,
 * Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * The interactive user interfaces in modified source and object code
 * versions of this program must display Appropriate Legal Notices,
 * as required under Section 5 of the GNU Affero General Public License.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public
 * License, these Appropriate Legal Notices must retain the display of the
 * "Powered by xbib" logo. If the display of the logo is not reasonably
 * feasible for technical reasons, the Appropriate Legal Notices must display
 * the words "Powered by xbib".
 */
package org.xbib.query.cql.model;

import org.xbib.query.cql.AbstractNode;
import org.xbib.query.cql.BooleanOperator;
import org.xbib.query.cql.SyntaxException;
import org.xbib.query.cql.Term;
import org.xbib.xml.namespace.XmlNamespaceContext;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * A CQL query model.
 *
 * Special index names are <code>facet</code>, <code>filter</code>,
 * and <code>option</code>.
 *
 * These indexes form breadcrumb trails.
 *
 * Bread crumbs provide a means for a server to track an chronologically
 * ordered set of client actions. Bread crumbs are typically rendered as a
 * user-driven constructed list of links, and are useful when
 * users select them to drill down and up in a structure,
 * so that they can find their way and have a notion of where they
 * currently are.
 *
 * Bread crumbs in the original sense just represent where users are
 * situated in a site hierarchy. For example, when browsing a
 * library catalog, bread crumbs could look like this:
 *
 * <pre>
 *  Home > Scientific literature > Arts & Human > Philosophy
 * </pre>
 *
 * or
 *
 * <pre>
 *   Main library > Branch library > First floor > Rare book room
 * </pre>
 *
 * These items would be rendered as links to the corresponding location.
 * Classes that implement this interface are responsible for managing
 * such a bread crumb structure. A typical implementation regards
 * bread crumbs as a set of elements.
 *
 * When a bread crumb is activated that was not in the set yet,
 * it would add it to the set, or when a bread crumb is activated
 * that is already on the set, it would roll back to the corresponding depth.
 *
 * In this model, multiple bread crumb trails may exist side by side. They are
 * separate and do not depend on each other. There is a list of bread crumb
 * trails, and the notion of a currently active bread crumb within a trail.
 *
 * This model does not make any presumptions on how it should interact with
 * breadcrumbs except that a breadcrumb model should be serializable into
 * a writer.
 *
 */
public final class CQLQueryModel {

    /** virtual namespace contexts 'facet', 'filter', and 'option' */
    public static final String FACET_INDEX_NAME = "facet";

    public static final String FILTER_INDEX_NAME = "filter";

    public static final String OPTION_INDEX_NAME = "option";

    private static final String AND_OP = " and ";

    private static final String OR_OP = " or ";

    /** the CQL context definitions */
    private static final Map<String, ResourceBundle> contexts = new HashMap();

    /** the namespace context for this CQL generator */
    private final XmlNamespaceContext nsContext = XmlNamespaceContext.getDefaultInstance() ;

    /** the CQL query string*/
    private String query;

    /** breadcrumb trail for facets*/
    private FacetBreadcrumbTrail facetTrail;

    /** breadcrumb trail for conjunctive filters  */
    private FilterBreadcrumbTrail conjunctivefilterTrail;

    /** breadcrumb trail for disjunctive filters */
    private FilterBreadcrumbTrail disjunctivefilterTrail;

    /** breadcrumb trail for options */
    private OptionBreadcrumbTrail optionTrail;

    public CQLQueryModel() {
        this.facetTrail = new FacetBreadcrumbTrail();
        this.conjunctivefilterTrail = new FilterBreadcrumbTrail(BooleanOperator.AND);
        this.disjunctivefilterTrail = new FilterBreadcrumbTrail(BooleanOperator.OR);
        this.optionTrail = new OptionBreadcrumbTrail();
        // load contexts
        Map<String, String> map = nsContext.getNamespaces();
        for (String context : map.keySet()) {
            if (!FACET_INDEX_NAME.equals(context)
                    && !FILTER_INDEX_NAME.equals(context)
                    && !OPTION_INDEX_NAME.equals(context)
                    && !contexts.containsKey(context)) {
                contexts.put(context, ResourceBundle.getBundle("org.xbib.query.cql." + context));
            }
        }
    }

    public XmlNamespaceContext getNamespaceContext() {
        return nsContext;
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
     * @param op boolean operator, AND for conjunctive filter, OR for disjunctive filter
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
     * Get visibility of a given context
     * @param context
     * @return
     */
    public static boolean isVisible(String context) {
        return !isFacetContext(context)
                && !isFilterContext(context)
                && !isOptionContext(context);
    }

    /**
     * Check if this context is the facet context
     * @param context
     * @return
     */
    public static boolean isFacetContext(String context) {
        return FACET_INDEX_NAME.equals(context);
    }

    /**
     * Check if this context is the filter context
     * @param context
     * @return
     */
    public static boolean isFilterContext(String context) {
        return FILTER_INDEX_NAME.equals(context);
    }

    /**
     * Check if this context is the option context
     * @param context
     * @return
     */
    public static boolean isOptionContext(String context) {
        return OPTION_INDEX_NAME.equals(context);
    }

    /**
     * Return field for an index name
     * @param context the context name
     * @param indexName the index name
     * @return the field name
     */
    public static String getFieldOfIndex(String context, String indexName) {
        try {
            return contexts.get(context).getString(indexName);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Check for valid context and index name. If  context is
     * facet/filter/option, do not check index. Otherwise, the query index
     * names must be declared in the namespace properties.
     *
     * @param context the context name to check
     * @param index the index of the context to check
     */
    public void checkValidIndex(String context, String index) {
        if (isFacetContext(context) || isFilterContext(context) || isOptionContext(context)) {
            return;
        }
        String uri = getNamespaceContext().getNamespaceURI(context);
        if (uri == null) {
            throw new SyntaxException("undeclared prefix " + context);
        }
        try {
            if (contexts.get(context) != null) {
                contexts.get(context).getString(index);
                return;
            }
        } catch (Exception e) {
            throw new SyntaxException("undeclared index '" + index
                    + "' in context '" + context
                    + "', valid are: " + contexts.get(context));
        }
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

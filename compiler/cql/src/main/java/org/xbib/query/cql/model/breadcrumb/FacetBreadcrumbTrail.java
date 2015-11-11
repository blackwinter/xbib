package org.xbib.query.cql.model.breadcrumb;

import org.xbib.query.cql.model.Facet;

import java.util.Iterator;
import java.util.TreeSet;

/**
 * Facet breadcrumb trail
 */
public class FacetBreadcrumbTrail extends TreeSet<Facet> {

    @Override
    public String toString() {
        return toCQL();
    }

    public String toCQL() {
        StringBuilder sb = new StringBuilder();
        if (isEmpty()) {
            return sb.toString();
        }
        Iterator<Facet> it = iterator();
        if (it.hasNext()) {
            sb.append(it.next().toCQL());
        }
        while (it.hasNext()) {
            sb.append(" and ").append(it.next().toCQL());
        }
        return sb.toString();
    }
}

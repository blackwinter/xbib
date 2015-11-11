package org.xbib.query.cql.model.breadcrumb;

import org.xbib.query.cql.BooleanOperator;
import org.xbib.query.cql.model.Filter;

import java.util.Iterator;
import java.util.TreeSet;

/**
 * Filter breadcrumbs
 */
public class FilterBreadcrumbTrail extends TreeSet<Filter> {

    private BooleanOperator op;

    public FilterBreadcrumbTrail(BooleanOperator op) {
        super();
        this.op = op;
    }

    @Override
    public String toString() {
        return toCQL();
    }

    public String toCQL() {
        StringBuilder sb = new StringBuilder();
        if (isEmpty()) {
            return sb.toString();
        }
        if (op == BooleanOperator.OR && size() > 1) {
            sb.append('(');
        }
        Iterator<Filter> it = this.iterator();
        sb.append(it.next().toCQL());
        while (it.hasNext()) {
            sb.append(' ').append(op).append(' ').append(it.next().toCQL());
        }
        if (op == BooleanOperator.OR && size() > 1) {
            sb.append(')');
        }
        return sb.toString();
    }
}

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

import org.xbib.query.BreadcrumbTrail;
import org.xbib.query.cql.BooleanOperator;

import java.util.Iterator;
import java.util.TreeSet;

/**
 * Filter breadcrumbs
 */
public class FilterBreadcrumbTrail extends TreeSet<Filter>
        implements BreadcrumbTrail<Filter> {

    private BooleanOperator op;
    /**
     * Constructor
     */
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

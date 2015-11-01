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

import org.xbib.query.Breadcrumb;
import org.xbib.query.QueryFilter;
import org.xbib.query.cql.Comparitor;

public class Filter<V> implements QueryFilter<V> {

    private String name;
    private V value;
    private Comparitor op;
    private String label;

    public Filter(String name, V value, Comparitor op) {
        this.name = name;
        this.op = op;
        this.value = value;
    }

    public Filter(String name, V value, Comparitor op, String label) {
        this.name = name;
        this.op = op;
        this.value = value;
        this.label = label;
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

    public Comparitor getFilterOperation() {
        return op;
    }

    public String getLabel() {
        return label;
    }

    public String toCQL() {
        return CQLQueryModel.FILTER_INDEX_NAME + "." + name + " " + op.getToken() + " " + value;
    }

    public int compareTo(Breadcrumb o) {
        return toString().compareTo(((Filter<V>)o).toString());
    }

    @Override
    public String toString() {
        return name + " " + op + " " + value;
    }
}

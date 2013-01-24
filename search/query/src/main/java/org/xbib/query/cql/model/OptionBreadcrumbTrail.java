/*
 * Licensed to Jörg Prante and xbib under one or more contributor 
 * license agreements. See the NOTICE.txt file distributed with this work
 * for additional information regarding copyright ownership.
 * 
 * Copyright (C) 2012 Jörg Prante and xbib
 * 
 * This program is free software; you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation; either version 3 of the License, or 
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License 
 * along with this program; if not, see http://www.gnu.org/licenses/
 *
 */
package org.xbib.query.cql.model;

import java.util.Iterator;
import java.util.TreeSet;
import org.xbib.query.BreadcrumbTrail;

/**
 * An Option breadcrumb trail is a trail of attributes (key/value pairs).
 * There is no interdependency between attributes; all values are allowed,
 * even if they interfere with each other, the trail does not resolve it.
 *
 * @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public class OptionBreadcrumbTrail extends TreeSet<Option>
        implements BreadcrumbTrail<Option> {

    @Override
    public String toString() {
        return toCQL();
    }

    /**
     * Conjunct all CQL options to form a valid CQL string.
     * 
     * @return the CQL string
     */
    public String toCQL() {
        StringBuilder sb = new StringBuilder();
        if (isEmpty()) {
            return sb.toString();
        }
        Iterator<Option> it = iterator();
        if (it.hasNext()) {
            sb.append(it.next().toCQL());
        }
        while (it.hasNext()) {
            sb.append(" and ").append(it.next().toCQL());
        }
        return sb.toString();
    }
}

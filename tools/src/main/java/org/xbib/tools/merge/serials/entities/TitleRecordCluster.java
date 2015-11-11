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
package org.xbib.tools.merge.serials.entities;

import org.xbib.tools.merge.serials.support.StatCounter;
import org.xbib.util.MultiMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class TitleRecordCluster {

    private final static Integer currentYear = GregorianCalendar.getInstance().get(GregorianCalendar.YEAR);

    private Integer firstDate = Integer.MAX_VALUE;

    private Integer lastDate = Integer.MIN_VALUE;

    private Set<TitleRecord> main = new TreeSet<>();

    private Set<TitleRecord> other = new TreeSet<>();

    private List<Expression> expressions = new ArrayList<>();

    private WorkSet workSet = new WorkSet();

    public void addMain(Collection<TitleRecord> main) {
        this.main.addAll(main);
        // expressions
        findExpressions();
        // works
        if (!expressions.isEmpty()) {
            StatCounter statCounter = new StatCounter();
            // set defaults
            statCounter.set("stat", "manifestations", 0);
            statCounter.set("stat", "holdings", 0);
            statCounter.set("stat", "volumes", 0);
            statCounter.set("stat", "services", 0);
            this.workSet = new WorkSet(expressions, statCounter);
        }
    }

    public void addOther(Collection<TitleRecord> other) {
        this.other.addAll(other);
    }

    public Collection<TitleRecord> getMain() {
        return main;
    }

    public Collection<TitleRecord> getOther() {
        return other;
    }

    public List<Expression> getExpressions() {
        return expressions;
    }

    public WorkSet getWorkSet() {
        return workSet;
    }

    public Collection<TitleRecord> getAll() {
        Set<TitleRecord> set = new TreeSet<>();
        set.addAll(main);
        set.addAll(other);
        return set;
    }

    public Integer getFirstDate() {
        return firstDate;
    }

    public Integer getLastDate() {
        return lastDate;
    }

    private void findExpressions() {
        List<TitleRecord> headRecords = new ArrayList<>();
        // special case of single element
        if (main.size() == 1) {
            TitleRecord tr = main.iterator().next();
            headRecords.add(tr);
            this.firstDate = tr.firstDate();
            this.lastDate = tr.lastDate();
        } else {
            for (TitleRecord m : this.main) {
                if (m.firstDate() == null) {
                    continue;
                }
                if (m.firstDate() < this.firstDate) {
                    this.firstDate = m.firstDate();
                }
                int d = m.lastDate() == null ? currentYear : m.lastDate();
                if (d > this.lastDate) {
                    this.lastDate = d;
                }
                // we have to check all related title records for first/last dates
                MultiMap<String,TitleRecord> mm  = m.getRelated();
                for (String key : mm.keySet()) {
                    for (TitleRecord p : mm.get(key)) {
                        if (p.firstDate() != null && p.firstDate() < firstDate) {
                            this.firstDate = p.firstDate();
                        }
                        d = p.lastDate() == null ? currentYear : p.lastDate();
                        if (d > this.lastDate) {
                            this.lastDate = d;
                        }
                    }
                }
                // Find all "head record" which qualifies for a "work".
                // - no monographs connected to serial
                // - no series connected to serial
                if (!m.isMonographic() && !m.isSubseries()) {
                    Set<String> rel = m.getRelations().keySet();
                    if (!rel.contains("isTransientEditionOf") && !rel.contains("succeededBy")) {
                        if (m.isPrint() || (m.isOnline() && !m.hasPrint())) {
                            // here we have works for all languages
                            headRecords.add(m);
                        }
                    }
                }
            }
        }
        if (firstDate != null && firstDate == Integer.MAX_VALUE) {
            // no first/last date found, example "Produkt-ISIL" ZDB-ID 21543057
            firstDate = null;
            lastDate = null;
        }
        if (lastDate != null && lastDate == Integer.MIN_VALUE) {
            // no last date found, assume current year
            lastDate = null;
        }
        // create expressions
        for (TitleRecord tr : headRecords) {
            Expression expression = new Expression(tr, this);
            expressions.add(expression);
        }
    }
}

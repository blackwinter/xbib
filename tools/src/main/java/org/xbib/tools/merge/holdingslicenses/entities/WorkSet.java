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
package org.xbib.tools.merge.holdingslicenses.entities;

import org.xbib.common.xcontent.ToXContent;
import org.xbib.common.xcontent.XContentBuilder;
import org.xbib.tools.merge.holdingslicenses.support.StatCounter;

import java.io.IOException;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class WorkSet {

    private final static Integer currentYear = GregorianCalendar.getInstance().get(GregorianCalendar.YEAR);

    private final List<Expression> expressions = new LinkedList<>();

    private List<Expression> works = new LinkedList<>();

    private StatCounter statCounter;

    public WorkSet() {
    }

    public WorkSet(List<Expression> expressions, StatCounter statCounter) {
        this.expressions.addAll(expressions);
        this.statCounter = statCounter;
        findWorks();
    }

    public List<Expression> getWorks() {
        return works;
    }

    public List<Expression> getExpressions() {
        return expressions;
    }

    private void findWorks() {
        if (expressions.size() == 1) {
            this.works = expressions;
            return; // single work, single expression
        }
        // strategy: sort by date order descending
        Collections.sort(expressions, (o1, o2) -> {
            Integer i1 = o1.getTitleRecord().firstDate();
            if (i1 == null) {
                i1 = 0;
            }
            Integer i2 = o2.getTitleRecord().firstDate();
            if (i2 == null) {
                i2 = 0;
            }
            if (i2 < i1) {
                return -1;
            } else if (i2 > i1) {
                return +1;
            } else {
                return 0;
            }
        });
        // take all expressions with same year as works
        Integer workdate = expressions.get(0).getTitleRecord().firstDate(); // anchor date
        if (workdate == null) {
            workdate = currentYear;
        }
        statCounter = new StatCounter();
        statCounter.set("stat", "manifestations", 0);
        statCounter.set("stat", "holdings", 0);
        statCounter.set("stat", "volumes", 0);
        statCounter.set("stat", "services", 0);
        Iterator<Expression> it = expressions.iterator();
        while (it.hasNext()) {
            Expression expression = it.next();
            if (expression.getTitleRecord().firstDate().equals(workdate)) {
                works.add(expression);
                statCounter.merge(expression.getStatCounter());
                // count expression languages
                statCounter.increase("lang", expression.getTitleRecord().language(), 1);
                it.remove(); // remove work from expressions
            }
        }
        // add relationships
        for (int i = 1; i < expressions.size(); i++) {
            for (Expression work : works) {
                expressions.get(i).getTitleRecord().addRelated("hasWork", work.getTitleRecord());
                work.getTitleRecord().addRelated("hasExpression", expressions.get(i).getTitleRecord());
            }
        }
    }

    public void toXContent(XContentBuilder builder, ToXContent.Params params) throws IOException {
        builder.startObject();
        // struct counter
        builder.startObject("count");
        for (Map.Entry<String,Map<String,Integer>> entry : statCounter.entrySet()) {
            String key = entry.getKey();
            Map<String,Integer> map = entry.getValue();
            builder.startObject(key);
            for (Map.Entry<String,Integer> entry2 : map.entrySet()) {
                String key2 = entry2.getKey();
                Integer value2 = entry2.getValue();
                builder.field(key2, value2);
            }
            builder.endObject();
        }
        builder.endObject();
        builder.field("workcount", works.size());
        builder.startArray("work");
        for (Expression work : works) {
            builder.startObject()
                    .field("identifierForTheWork", work.getTitleRecord().externalID)
                    .field("title", work.getTitleRecord().getTitleComponents())
                    .field("firstdate", work.getTitleRecord().firstDate())
                    .field("lastdate", work.getTitleRecord().lastDate())
                    .field("language", work.getTitleRecord().language())
                    .field("country", work.getTitleRecord().country())
                    .endObject();
        }
        builder.endArray();
        builder.field("expressioncount", expressions.size());
        builder.startArray("expression");
        for (Expression expression : expressions) {
            builder.startObject()
                    .field("identifierForTheExpression", expression.getTitleRecord().externalID)
                    .field("title", expression.getTitleRecord().getTitleComponents())
                    .field("firstdate", expression.getTitleRecord().firstDate())
                    .field("lastdate", expression.getTitleRecord().lastDate())
                    .field("language", expression.getTitleRecord().language())
                    .field("country", expression.getTitleRecord().country())
                    .endObject();
        }
        builder.endArray();
        builder.endObject();
    }

    @Override
    public String toString() {
         //first work only
        return works.iterator().next().getTitleRecord().externalID();
    }
}

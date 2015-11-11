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
package org.xbib.analyzer.marc.zdb.hol;

import org.xbib.etl.marc.MARCEntity;
import org.xbib.etl.marc.MARCEntityQueue;
import org.xbib.etl.support.EnumerationAndChronologyHelper;
import org.xbib.marc.FieldList;
import org.xbib.marc.Field;
import org.xbib.rdf.Resource;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class TextualHoldings extends MARCEntity {

    private final static TextualHoldings instance = new TextualHoldings();

    public static TextualHoldings getInstance() {
        return instance;
    }

    private Pattern[] movingwallPatterns;

    @SuppressWarnings("unchecked")
    @Override
    public TextualHoldings setSettings(Map<String,Object> params) {
        super.setSettings(params);
        List<String> movingwalls = (List<String>) params.get("movingwall");
        if (movingwalls != null) {
            Pattern[] p = new Pattern[movingwalls.size()];
            for (int i = 0; i < movingwalls.size(); i++) {
                p[i] = Pattern.compile(movingwalls.get(i));
            }
            setMovingwallPatterns(p);
        }
        return this;
    }

    public void setMovingwallPatterns(Pattern[] p) {
        this.movingwallPatterns = p;
    }

    public Pattern[] getMovingwallPatterns() {
        return this.movingwallPatterns;
    }

    @Override
    public boolean fields(MARCEntityQueue.MARCWorker worker,
                          FieldList fields, String value) throws IOException {
        EnumerationAndChronologyHelper eac = new EnumerationAndChronologyHelper();
        for (Field field : fields) {
            String data = field.data();
            if (data == null || data.isEmpty()) {
                continue;
            }
            worker.state().getResource().add("textualholdings", data);
            if ("a".equals(field.subfieldId())) {
                Resource r = worker.state().getResource().newResource("holdings");
                Resource parsedHoldings = eac.parse(data, r, getMovingwallPatterns());
                if (!parsedHoldings.isEmpty()) {
                    Set<Integer> dates = eac.dates(r.id(), parsedHoldings);
                    for (Integer date : dates) {
                        worker.state().getResource().add("dates", date);
                    }
                } else {
                    if (logger.isTraceEnabled()) {
                        logger.trace("no dates found in field " + field);
                    }
                }
            }
        }
        return false;
    }

}

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
package org.xbib.analyzer.mab.titel;

import org.xbib.etl.faceting.TermFacet;
import org.xbib.etl.marc.dialects.mab.MABEntity;
import org.xbib.etl.marc.dialects.mab.MABEntityQueue;
import org.xbib.marc.FieldList;
import org.xbib.rdf.Literal;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TypeMediaSpecial extends MABEntity {

    private String facet = "dc.format";

    private String predicate;

    private Map<Pattern,String> patterns;

    public TypeMediaSpecial(Map<String,Object> params) {
        super(params);
        if (params.containsKey("_facet")) {
            this.facet = params.get("_facet").toString();
        }
        this.predicate = this.getClass().getSimpleName();
        if (params.containsKey("_predicate")) {
            this.predicate = params.get("_predicate").toString();
        }
        Map<String, Object> regexes = (Map<String, Object>) getParams().get("regexes");
        if (regexes != null) {
            patterns = new HashMap<>();
            for (Map.Entry<String, Object> entry : regexes.entrySet()) {
                String key = entry.getKey();
                patterns.put(Pattern.compile(Pattern.quote(key), Pattern.CASE_INSENSITIVE), (String) regexes.get(key));
            }
        }
    }

    @Override
    public boolean fields(MABEntityQueue.MABWorker worker, FieldList fields) throws IOException {
        String value = fields.getLast().data();
        for (String code : findCodes(value)) {
            worker.getWorkerState().getResource().add(predicate, code);
            // facetize here, so we have to find codes only once
            if (worker.getWorkerState().getFacets().get(facet) == null) {
                worker.getWorkerState().getFacets().put(facet, new TermFacet().setName(facet).setType(Literal.STRING));
            }
            worker.getWorkerState().getFacets().get(facet).addValue(code);
        }
        return true; // done!
    }

    private List<String> findCodes(String value) {
        List<String> list = new LinkedList<>();
        Map<String, Object> rak = (Map<String, Object>) getParams().get("rak");
        if (rak != null && rak.containsKey(value)) {
            list.add((String) rak.get(value));
        }
        if (patterns != null) {
            // pattern matching
            for (Map.Entry<Pattern,String> entry : patterns.entrySet()) {
                Pattern p = entry.getKey();
                Matcher m = p.matcher(value);
                if (m.find()) {
                    String v = patterns.get(p);
                    if (v != null) {
                        list.add(v);
                    }
                }
            }
        }
        if (list.isEmpty()) {
            logger.warn("no media type detected from value: \"{}\"", value);
        }
        return list;
    }

}

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

import org.xbib.entities.faceting.StringFacet;
import org.xbib.entities.marc.dialects.mab.MABEntity;
import org.xbib.entities.marc.dialects.mab.MABEntityQueue;
import org.xbib.marc.FieldList;
import org.xbib.rdf.Literal;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.collect.Lists.newLinkedList;

public class TypeMedia extends MABEntity {

    private final static TypeMedia element = new TypeMedia();

    public static TypeMedia getInstance() {
        return element;
    }

    private String facet = "dc.format";

    private String predicate;

    private final Map<Pattern,String> patterns = new HashMap<Pattern,String>();

    @Override
    public MABEntity setSettings(Map params) {
        super.setSettings(params);
        if (params.containsKey("_facet")) {
            this.facet = params.get("_facet").toString();
        }
        this.predicate = this.getClass().getSimpleName();
        if (params.containsKey("_predicate")) {
            this.predicate = params.get("_predicate").toString();
        }
        Map<String, Object> regexes = (Map<String, Object>) getSettings().get("regexes");
        if (regexes != null) {
            synchronized (patterns) {
                for (String key : regexes.keySet()) {
                    patterns.put(Pattern.compile(Pattern.quote(key), Pattern.CASE_INSENSITIVE), (String) regexes.get(key));
                }
            }
        }
        return this;
    }

    @Override
    public boolean fields(MABEntityQueue.MABWorker worker,
                          FieldList fields, String value) throws IOException {
        if (value == null || value.isEmpty()) {
            value = fields.getLast().data();
        }
        for (String code : findCodes(value)) {
            worker.state().getResource().add(predicate, code);
            // facetize here, so we have to find codes only once
            if (worker.state().getFacets().get(facet) == null) {
                worker.state().getFacets().put(facet, new StringFacet().setName(facet).setType(Literal.STRING));
            }
            worker.state().getFacets().get(facet).addValue(code);
        }
        return true; // done!
    }

    private List<String> findCodes(String value) {
        boolean isRAK = false;
        List<String> list = newLinkedList();
        Map<String, Object> rak = (Map<String, Object>) getSettings().get("rak");
        if (rak != null && rak.containsKey(value)) {
            list.add((String) rak.get(value));
            isRAK = true;
        }
        synchronized (patterns) {
            // pattern matching
            for (Pattern p : patterns.keySet()) {
                Matcher m = p.matcher(value);
                if (m.find()) {
                    String v = patterns.get(p);
                    if (v != null) {
                        list.add(v);
                    }
                }
            }
        }
        if (!isRAK && !list.isEmpty()) {
            logger.warn("additional media types {} detected from value: \"{}\"", list, value);
        }
        if (list.isEmpty()) {
            logger.warn("no media type detected from value: \"{}\"", value);
        }
        return list;
    }
}

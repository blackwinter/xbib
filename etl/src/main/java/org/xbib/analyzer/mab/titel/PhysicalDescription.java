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
import org.xbib.etl.marc.dialects.mab.MABEntityBuilderState;
import org.xbib.etl.marc.dialects.mab.MABEntityQueue;
import org.xbib.marc.FieldList;
import org.xbib.rdf.Literal;

import java.io.IOException;
import java.util.Map;

/**
 * http://www.dnb.de/SharedDocs/Downloads/DE/DNB/standardisierung/protokolle/pEgDf20050411v.pdf?__blob=publicationFile
 *
 * http://www.bsb-muenchen.de/fileadmin/imageswww/pdf-dateien/abteilungen/Schule/RAK-NBM-BVB-ALEPH-2014-03_Handout.pdf
 */
public class PhysicalDescription extends MABEntity {

    public final static String FACET = "dc.format";

    private String predicate;

    private Map<String, Object> codes;

    private Map<String, Object> facetcodes;

    public PhysicalDescription(Map<String,Object> params) {
        super(params);
        this.predicate = this.getClass().getSimpleName();
        if (params.containsKey("_predicate")) {
            this.predicate = params.get("_predicate").toString();
        }
        this.codes = (Map<String, Object>) getParams().get("codes");
        this.facetcodes = (Map<String, Object>) getParams().get("facetcodes");
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean fields(MABEntityQueue.MABWorker worker, FieldList fields) throws IOException {
        String value = fields.getLast().data();
        if (codes != null) {
            for (int i = 0; i < value.length(); i++) {
                Map<String, Object> q = (Map<String, Object>) codes.get(Integer.toString(i));
                if (q != null) {
                    String code = (String) q.get(value.substring(i, i + 1));
                    if (code == null && i + 1 < value.length()) {
                        // two letters?
                        code = (String) q.get(value.substring(i, i + 2));
                    }
                    worker.state().getResource().add(predicate, code);
                }
            }
        }
        if (facetcodes != null) {
            for (int i = 0; i < value.length(); i++) {
                Map<String, Object> q = (Map<String, Object>) facetcodes.get(Integer.toString(i));
                if (q != null) {
                    String code = (String) q.get(value.substring(i, i + 1));
                    if (code == null && i + 1 < value.length()) {
                        // two letters?
                        code = (String) q.get(value.substring(i, i + 2));
                    }
                    facetize(worker.state(), code);
                }
            }
        }
        return true; // done
    }

    private MABEntity facetize(MABEntityBuilderState state, String value) {
        if (value != null && !value.isEmpty()) {
            if (state.getFacets().get(FACET) == null) {
                state.getFacets().put(FACET, new TermFacet().setName(FACET).setType(Literal.STRING));
            }
            state.getFacets().get(FACET).addValue(value);
        }
        return this;
    }

}

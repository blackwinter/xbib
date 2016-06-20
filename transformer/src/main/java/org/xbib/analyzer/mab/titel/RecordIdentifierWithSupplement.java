/*
 * Licensed to Jörg Prante and xbib under one or more contributor
 * license agreements. See the NOTICE.txt file distributed with this work
 * for additional information regarding copyright ownership.
 *
 * Copyright (C) 2016 Jörg Prante and xbib
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
 *
 * Created by Philipp v. Böselager on 20.06.2016.
 */
package org.xbib.analyzer.mab.titel;

import org.xbib.etl.faceting.TermFacet;
import org.xbib.etl.marc.dialects.mab.MABEntityBuilderState;
import org.xbib.etl.marc.dialects.mab.MABEntityQueue;
import org.xbib.etl.support.Supplement;
import org.xbib.etl.support.SupplementEntry;
import org.xbib.rdf.Literal;
import org.xbib.rdf.Resource;

import java.util.List;
import java.util.Map;

public class RecordIdentifierWithSupplement extends RecordIdentifier {

    public RecordIdentifierWithSupplement(Map<String, Object> params) {
        super(params);
    }

    @Override
    public String data(MABEntityQueue.MABWorker worker,
                       String predicate, Resource resource, String property, String value) {
        MABEntityBuilderState state = worker.newState();
        // check for supplements
        List<Supplement> supplements = worker.supplements();
        if (supplements != null || !supplements.isEmpty()) {
            String isil = catalogid;
            String key = catalogid + "." + state.getRecordIdentifier() + ".";
            for (Supplement supplement : supplements) {
                java.util.Collection<SupplementEntry> entries = supplement.lookup(key);
                if (entries != null) {
                    for (SupplementEntry entry : entries) {

                        // TODO: transfer data from entry to state.getFacets properly

                        if (entry.get("") != null && !entry.get("").trim().isEmpty()) {
                            String facet = taxonomyFacet + "." + isil + ".notation";
                            state.getFacets().putIfAbsent(facet, new TermFacet().setName(facet).setType(Literal.STRING));
                            state.getFacets().get(facet).addValue(entry.get(""));
                        }
                    }
                }
            }
        }
        return super.data(worker, predicate, resource, property, value);
    }

}

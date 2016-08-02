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

import org.xbib.etl.marc.dialects.mab.MABEntityBuilderState;
import org.xbib.etl.marc.dialects.mab.MABEntityQueue;
import org.xbib.etl.support.Supplement;
import org.xbib.etl.support.SupplementEntry;
import org.xbib.iri.IRI;
import org.xbib.rdf.Resource;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class RecordIdentifierWithSupplement extends RecordIdentifier {

    public RecordIdentifierWithSupplement(Map<String, Object> params) {
        super(params);
        if (params.containsKey("catalogid")) {
            this.catalogid = params.get("catalogid").toString();
            this.prefix = "(" + this.catalogid + ")";
        }
    }

    @Override
    public String data(MABEntityQueue.MABWorker worker,
                       String predicate, Resource resource, String property, String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        final MABEntityBuilderState state = worker.getWorkerState();
        final String trimmed = value.trim();
        final String v = prefix + trimmed;
        worker.getWorkerState().setRecordIdentifier(v);
        // check for supplements
        final List<Supplement> supplements = worker.supplements();
        if (supplements != null && !supplements.isEmpty()) {
            for (Supplement supplement : supplements) {
                final Map<String, String> mappings = supplement.getMappings();
                java.util.Collection<SupplementEntry> entries = supplement.lookup(trimmed);
                if (entries != null) {
                    for (SupplementEntry entry : entries) {
                        for (Map.Entry<String, String> supplementMapping : mappings.entrySet()) {
                            final String key = entry.get(supplementMapping.getKey());
                            final String info = entry.get(key);
                            try {
                                final java.util.Collection<Resource> embedded = state.getResource().embeddedResources(new IRI("tmp"));
                                for (Resource embed : embedded){
                                    embed.add(supplementMapping.getValue(), info);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
        return super.data(worker, predicate, resource, property, value);
    }

}

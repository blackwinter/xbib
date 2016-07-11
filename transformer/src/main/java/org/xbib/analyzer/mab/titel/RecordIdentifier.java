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
import org.xbib.etl.support.ConfigurableClassifier;
import org.xbib.etl.support.ClassifierEntry;
import org.xbib.rdf.Literal;
import org.xbib.rdf.Resource;

import java.io.IOException;
import java.util.Map;

public class RecordIdentifier extends MABEntity {

    protected final static String taxonomyFacet = "xbib.taxonomy";

    private String prefix = "";

    protected String catalogid = "";

    public RecordIdentifier(Map<String,Object> params) {
        super(params);
        if (params.containsKey("_prefix")) {
            this.prefix = params.get("_prefix").toString();
        }
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
        MABEntityBuilderState state = worker.getWorkerState();
        String v = prefix + value.trim();
        worker.getWorkerState().setIdentifier(v);
        worker.getWorkerState().setRecordIdentifier(v);
        try {
            worker.getWorkerState().getResource().newResource("xbib").add("uid", v);
        } catch (IOException e) {
            // ignore
        }
        // check for classifier
        ConfigurableClassifier classifier = worker.classifier();
        if (classifier != null) {
            String isil = catalogid;
            String key = catalogid + "." + state.getRecordIdentifier() + ".";
            java.util.Collection<ClassifierEntry> entries = classifier.lookup(key);
            if (entries != null) {
                for (ClassifierEntry classifierEntry : entries) {
                    if (classifierEntry.getCode() != null && !classifierEntry.getCode().trim().isEmpty()) {
                        String facet = taxonomyFacet + "." + isil + ".notation";
                        state.getFacets().putIfAbsent(facet, new TermFacet().setName(facet).setType(Literal.STRING));
                        state.getFacets().get(facet).addValue(classifierEntry.getCode());
                    }
                    if (classifierEntry.getText() != null && !classifierEntry.getText().trim().isEmpty()) {
                        String facet = taxonomyFacet + "." + isil + ".text";
                        state.getFacets().putIfAbsent(facet, new TermFacet().setName(facet).setType(Literal.STRING));
                        state.getFacets().get(facet).addValue(classifierEntry.getText());
                    }
                }
            }
        }
        return v;
    }
}

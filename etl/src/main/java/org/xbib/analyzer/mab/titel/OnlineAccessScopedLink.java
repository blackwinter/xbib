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
import org.xbib.iri.IRI;
import org.xbib.marc.FieldList;
import org.xbib.rdf.Literal;
import org.xbib.rdf.Resource;

import java.io.IOException;
import java.util.Map;

public class OnlineAccessScopedLink extends OnlineAccessRemote {

    private final static OnlineAccessScopedLink element = new OnlineAccessScopedLink();

    public static OnlineAccessScopedLink getInstance() {
        return element;
    }

    private final static String taxonomyFacet = "xbib.taxonomy";


    private String catalogid = "";

    @Override
    public MABEntity setSettings(Map<String,Object> params) {
        super.setSettings(params);
        // override by "catalogid"
        if (params.containsKey("catalogid")) {
            this.catalogid = params.get("catalogid").toString();
        }
        return this;
    }

    @Override
    public boolean fields(MABEntityQueue.MABWorker worker,
                          FieldList fields, String value) throws IOException {
        worker.addToResource(worker.state().getNextItemResource(), fields, this);
        return true;
    }

    @Override
    public String data(MABEntityQueue.MABWorker worker,
                       String predicate, Resource resource, String property, String value) {
        if (value == null) {
            return null;
        }
        MABEntityBuilderState state = worker.state();
        if ("url".equals(property)) {
            // create synthetic local record identifier
            state.setUID(IRI.builder().curie("uid:" + state.getRecordIdentifier() + "/" + state.getISIL() + "/" + value).build());
        } else if ("scope".equals(property) && catalogid != null && !catalogid.isEmpty()) {
            String isil = catalogid;
            resource.add("identifier", isil);
            ConfigurableClassifier classifier = worker.classifier();
            if (classifier != null) {
                String key = isil + "." + state.getRecordIdentifier() + ".";
                java.util.Collection<ConfigurableClassifier.Entry> entries = classifier.lookup(key);
                if (entries != null) {
                    for (ConfigurableClassifier.Entry entry : entries) {
                        String facet = taxonomyFacet + "." + isil + ".notation";
                        if (state.getFacets().get(facet) == null) {
                            state.getFacets().put(facet, new TermFacet().setName(facet).setType(Literal.STRING));
                        }
                        state.getFacets().get(facet).addValue(entry.getCode());
                        facet = taxonomyFacet + "." + isil + ".text";
                        if (state.getFacets().get(facet) == null) {
                            state.getFacets().put(facet, new TermFacet().setName(facet).setType(Literal.STRING));
                        }
                        state.getFacets().get(facet).addValue(entry.getText());
                    }
                }
            }
            return isil;
        }
        return value;
    }
}

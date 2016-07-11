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

import org.xbib.etl.faceting.Facet;
import org.xbib.etl.faceting.TermFacet;
import org.xbib.etl.marc.dialects.mab.MABEntity;
import org.xbib.etl.marc.dialects.mab.MABEntityBuilderState;
import org.xbib.etl.marc.dialects.mab.MABEntityQueue;
import org.xbib.etl.support.ClassifierEntry;
import org.xbib.etl.support.IdentifierMapper;
import org.xbib.etl.support.StatusCodeMapper;
import org.xbib.etl.support.ConfigurableClassifier;
import org.xbib.iri.IRI;
import org.xbib.marc.Field;
import org.xbib.marc.FieldList;
import org.xbib.rdf.Literal;
import org.xbib.rdf.Resource;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class Item extends MABEntity {

    /*
            "a": "identifier",
            "b": "shelfmark",
            "c": "callnumber",
            "d": "collection",
            "e": "status"
     */

    private String identifierFacet = "xbib.identifier";

    private final static String taxonomyFacet = "xbib.taxonomy";

    public Item(Map<String,Object> params) {
        super(params);
        // facet name
        if (params.containsKey("_facet")) {
            this.identifierFacet = params.get("_facet").toString();
        }
    }

    @Override
    public boolean fields(MABEntityQueue.MABWorker worker, FieldList fields) throws IOException {
        worker.addToResource(worker.getWorkerState().getNextItemResource(), fields, this);
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public String data(MABEntityQueue.MABWorker worker,
            String predicate, Resource resource, String property, String value) {
        if (value == null) {
            return null;
        }
        MABEntityBuilderState state = worker.getWorkerState();
        if ("identifier".equals(property)) {
            IdentifierMapper mapper = worker.identifierMapper();
            if (mapper != null) {
                String isil = mapper.lookup(value);
                if (isil != null) {
                    resource.add("identifier", isil);
                    state.setUID(IRI.builder().curie(isil).build());
                    state.setISIL(isil);
                    state.getFacets().putIfAbsent(identifierFacet, new TermFacet().setName(identifierFacet).setType(Literal.STRING));
                    Facet holderFacet = state.getFacets().get(identifierFacet);
                    holderFacet.addValue(isil);
                    // add "main ISIL" if not main ISIL (=two hyphens)
                    int pos = isil.lastIndexOf("-");
                    if (isil.indexOf("-") < pos) {
                        holderFacet.addValue(isil.substring(0, pos));
                    }
                    ConfigurableClassifier classifier = worker.classifier();
                    if (classifier != null) {
                        String doc = state.getRecordIdentifier();
                        java.util.Collection<ClassifierEntry> entries = classifier.lookup(isil, doc, value, null);
                        if (entries != null) {
                            for (ClassifierEntry classifierEntry : entries) {
                                String facet = taxonomyFacet + "." + isil + ".notation";
                                state.getFacets().putIfAbsent(facet, new TermFacet().setName(facet).setType(Literal.STRING));
                                state.getFacets().get(facet).addValue(classifierEntry.getCode());
                                facet = taxonomyFacet + "." + isil + ".text";
                                state.getFacets().putIfAbsent(facet, new TermFacet().setName(facet).setType(Literal.STRING));
                                state.getFacets().get(facet).addValue(classifierEntry.getText());
                            }
                        }
                    }
                    return isil;
                }
            }
        } else if ("callnumber".equals(property)) {
            // create synthetic local record identifier

            state.setUID(IRI.builder().curie(state.getISIL() + "/" + value).build());
            ConfigurableClassifier classifier = worker.classifier();
            if (classifier != null) {
                String isil = state.getISIL();
                String doc = state.getRecordIdentifier();
                java.util.Collection<ClassifierEntry> entries = classifier.lookup(isil, doc, value, null);
                if (entries != null) {
                    for (ClassifierEntry classifierEntry : entries) {
                        String facet = taxonomyFacet + "." + isil + ".notation";
                        state.getFacets().putIfAbsent(facet, new TermFacet().setName(facet).setType(Literal.STRING));
                        state.getFacets().get(facet).addValue(classifierEntry.getCode());
                        facet = taxonomyFacet + "." + isil + ".text";
                        state.getFacets().putIfAbsent(facet, new TermFacet().setName(facet).setType(Literal.STRING));
                        state.getFacets().get(facet).addValue(classifierEntry.getText());
                    }
                }
            }
        } else if ("status".equals(property)) {
            StatusCodeMapper mapper = worker.statusCodeMapper();
            if (mapper != null && mapper.getMap().containsKey(value)) {
                List<String> codes = (List<String>) mapper.getMap().get(value);
                for (String code : codes) {
                    resource.add("interlibraryservice", code);
                }
            }
        }
        return value;
    }

    @Override
    public MABEntity facetize(MABEntityQueue.MABWorker worker, Field field) {
        // empty, we do faceting in the data method
        return this;
    }

}

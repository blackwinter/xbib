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
package org.xbib.etl.marc.dialects.mab;

import org.xbib.etl.DefaultEntityBuilderState;
import org.xbib.etl.Specification;
import org.xbib.etl.faceting.Facet;
import org.xbib.etl.faceting.GregorianYearFacet;
import org.xbib.etl.sequencing.Sequence;
import org.xbib.iri.IRI;
import org.xbib.rdf.Literal;
import org.xbib.rdf.RdfContentBuilderProvider;
import org.xbib.rdf.RdfGraph;
import org.xbib.rdf.RdfGraphParams;
import org.xbib.rdf.Resource;
import org.xbib.rdf.memory.MemoryLiteral;
import org.xbib.rdf.memory.MemoryResource;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MABEntityBuilderState extends DefaultEntityBuilderState {

    private final IRI ITEM = IRI.create("item");

    private final static String LANGUAGE_FACET = "dc.language";
    private final static String DATE_FACET = "dc.date";
    private final static String TYPE_FACET = "dc.type";
    private final static String FORMAT_FACET = "dc.format";

    private final Map<String, Facet> facets;

    private final Map<String, Sequence> sequences;

    private final String packageName;

    private final Specification specification;

    private String systemIdentifier;

    private String recordIdentifier;

    private String format;

    private String type;

    private String label;

    private Resource root;

    private String isil;

    private IRI uid;

    public MABEntityBuilderState(String packageName, Specification specification, RdfGraph<RdfGraphParams> graph, Map<IRI,RdfContentBuilderProvider> providers) {
        super(graph, providers);
        this.packageName = packageName;
        this.specification = specification;
        this.facets = new HashMap<>();
        this.sequences = new HashMap<>();
    }

    public Resource getResource() throws IOException {
        if (!graph().getResources().hasNext()) {
            this.root = new MemoryResource().blank();
            graph().receive(root);
        }
        return this.root;
    }

    public Resource getResource(IRI predicate) throws IOException {
        if (!graph().hasResource(predicate)) {
            MemoryResource resource = new MemoryResource().blank();
            graph().putResource(predicate, resource);
        }
        return graph().getResource(predicate);
    }

    public Resource getNextItemResource() {
        if (graph().hasResource(ITEM)) {
            Resource resource = graph().removeResource(ITEM);
            resource.id(uid != null ? uid : resource.id());
            graph().putResource(resource.id(), resource);
        }
        uid = null;
        MemoryResource item = new MemoryResource().blank();
        graph().putResource(ITEM, item);
        return item;
    }

    public MABEntityBuilderState setIdentifier(String identifier) {
        this.systemIdentifier = identifier;
        return this;
    }

    public String getIdentifier() {
        return systemIdentifier;
    }

    public MABEntityBuilderState setRecordIdentifier(String identifier) {
        this.recordIdentifier = identifier;
        return this;
    }

    public String getRecordIdentifier() {
        return recordIdentifier;
    }

    public MABEntityBuilderState setFormat(String format) {
        this.format = format;
        return this;
    }

    public String getFormat() {
        return format;
    }

    public MABEntityBuilderState setType(String type) {
        this.type = type;
        return this;
    }

    public String getType() {
        return type;
    }

    public MABEntityBuilderState setLabel(String label) {
        this.label = label;
        return this;
    }

    public String getLabel() {
        return label;
    }

    public MABEntityBuilderState setISIL(String isil) {
        this.isil = isil;
        return this;
    }

    public String getISIL() {
        return isil;
    }

    public MABEntityBuilderState setUID(IRI uid) {
        this.uid = uid;
        return this;
    }

    public IRI getUID() {
        return uid;
    }

    public Map<String, Facet> getFacets() {
        return facets;
    }

    public Map<String, Sequence> getSequences() {
        return sequences;
    }

    @Override
    public void complete() throws IOException {
        if (getResource().isDeleted()) {
            // no facet logic / sequence logic for deleted records
            return;
        }

        // last item
        if (graph().hasResource(ITEM)) {
            Resource resource = graph().removeResource(ITEM);
            resource.id(uid != null ? uid : resource.id());
            graph().putResource(resource.id(), resource);
        }

        // create default facets
        Facet languageFacet = facets.get(LANGUAGE_FACET);
        if (languageFacet == null) {
            MABEntity entity = (MABEntity) specification.getEntities().get(packageName + ".Language");
            languageFacet = entity.getDefaultFacet();
            if (languageFacet != null) {
                facets.put(LANGUAGE_FACET, languageFacet);
            }
        }
        Facet formatFacet = facets.get(FORMAT_FACET);
        if (formatFacet == null) {
            MABEntity entity = (MABEntity) specification.getEntities().get(packageName + ".FormatCarrier");
            formatFacet = entity.getDefaultFacet();
            if (formatFacet != null) {
                facets.put(FORMAT_FACET, formatFacet);
            }
        }
        Facet typeFacet = facets.get(TYPE_FACET);
        if (typeFacet == null) {
            MABEntity entity = (MABEntity) specification.getEntities().get(packageName + ".TypeMonograph");
            typeFacet = entity.getDefaultFacet();
            if (typeFacet != null) {
                facets.put(TYPE_FACET, typeFacet);
            }
        }
        GregorianYearFacet dateFacet = (GregorianYearFacet) facets.get(DATE_FACET);
        if (dateFacet == null) {
            MABEntity entity = (MABEntity) specification.getEntities().get(packageName + ".Date");
            dateFacet = (GregorianYearFacet) entity.getDefaultFacet();
            if (dateFacet != null) {
                facets.put(DATE_FACET, dateFacet);
            }
        }

        for (Facet facet : facets.values()) {
            String facetName = facet.getName();
            if (facetName == null) {
                continue;
            }
            // split facet name e.g. "dc.date" --> "dc", "date"
            String[] facetPath = facetName.split("\\.");
            Resource resource = getResource();
            if (facetPath.length > 1) {
                for (int i = 0; i < facetPath.length - 1; i++) {
                    resource = resource.newResource(IRI.builder().path(facetPath[i]).build());
                }
            }
            facetName = facetPath[facetPath.length - 1];
            IRI predicate = IRI.builder().path(facetName).build();
            for (Object value : facet.getValues()) {
                Literal literal = new MemoryLiteral(value).type(facet.getType());
                try {
                    literal.object(); // provoke NumberFormatException for numerical values
                    resource.add(predicate, literal);
                } catch (Exception e) {
                    // if not valid, ignore value
                }
            }
        }
        facets.clear();

        // create sequences
        for (Sequence sequence : sequences.values()) {
            String sequenceName = sequence.getName();
            if (sequenceName == null) {
                continue;
            }
            // split sequence name e.g. "dc.subject" --> "dc", "subject"
            String[] sequencePath = sequenceName.split("\\.");
            Resource resource = getResource();
            if (sequencePath.length > 1) {
                for (int i = 0; i < sequencePath.length - 1; i++) {
                    resource = resource.newResource(IRI.builder().path(sequencePath[i]).build());
                }
            }
            sequenceName = sequencePath[sequencePath.length-1];
            IRI predicate = IRI.builder().path(sequenceName).build();
            for (Resource res : sequence.getResources()) {
                resource.add(predicate, res);
            }
        }
        sequences.clear();

        // continue with completion in parent
        super.complete();
    }
}

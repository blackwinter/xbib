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
package org.xbib.tools.feed.elasticsearch.dblp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.index.IndexAction;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.xbib.iri.IRI;
import org.xbib.iri.namespace.IRINamespaceContext;
import org.xbib.rdf.RdfConstants;
import org.xbib.rdf.RdfContentBuilder;
import org.xbib.rdf.RdfContentParams;
import org.xbib.rdf.content.RdfXContentParams;
import org.xbib.rdf.content.RouteRdfXContentParams;
import org.xbib.rdf.io.xml.AbstractXmlResourceHandler;
import org.xbib.rdf.io.xml.XmlContentParser;
import org.xbib.rdf.io.xml.XmlHandler;
import org.xbib.tools.convert.Converter;
import org.xbib.tools.feed.elasticsearch.Feeder;
import org.xbib.tools.input.FileInput;
import org.xbib.util.concurrent.WorkerProvider;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;

import static org.xbib.rdf.content.RdfXContentFactory.routeRdfXContentBuilder;

/**
 * Elasticsearch indexer for DBLP http://dblp.uni-trier.de/xml/
 */
public final class XML extends Feeder {

    private final static Logger logger = LogManager.getLogger(XML.class.getSimpleName());

    private final IRINamespaceContext namespaceContext = IRINamespaceContext.newInstance();

    @Override
    protected WorkerProvider<Converter> provider() {
        return p -> new XML().setPipeline(p);
    }

    @Override
    public void process(URI uri) throws Exception {
        try (InputStream in = FileInput.getInputStream(uri)) {
            namespaceContext.add(new HashMap<String, String>() {{
                put(RdfConstants.NS_PREFIX, RdfConstants.NS_URI);
                put("dc", "http://purl.org/dc/elements/1.1/");
                put("dcterms", "http://purl.org/dc/terms/");
                put("foaf", "http://xmlns.com/foaf/0.1/");
                put("frbr", "http://purl.org/vocab/frbr/core#");
                put("fabio", "http://purl.org/spar/fabio/");
                put("prism", "http://prismstandard.org/namespaces/basic/3.0/");
            }});

            RdfContentParams params = new RdfXContentParams(namespaceContext);
            DBLPHandler handler = new DBLPHandler(params);
            handler.setDefaultNamespace("dblp", "http://dblp.uni-trier.de/xml/");
            new XmlContentParser(in)
                    .setNamespaces(false)
                    .setHandler(handler)
                    .parse();
        }
    }

    private class DBLPHandler extends AbstractXmlResourceHandler {

        private final RouteRdfXContentParams params;

        private String id;

        DBLPHandler(RdfContentParams params) {
            super(params);
            this.params = new RouteRdfXContentParams(getNamespaceContext(),
                    indexDefinitionMap.get("bib").getConcreteIndex(),
                    indexDefinitionMap.get("bib").getType());
        }

        @Override
        public void startElement(String nsURI, String localname, String qname, Attributes atts) throws SAXException {
            super.startElement(nsURI, localname, qname, atts);
            if (atts != null) {
                int i = atts.getIndex("key");
                if (i >= 0) {
                    this.id = atts.getValue(i);
                    if (settings.get("identifier") != null) {
                        this.id = "(" + settings.get("identifier") + ")" + this.id;
                    }
                }
            }
        }

        /**
         * http://dblp.dagstuhl.de/xml/dblp.dtd
         * @param name name
         * @return true if resource delimiter
         */
        @Override
        public boolean isResourceDelimiter(QName name) {
            return "article".equals(name.getLocalPart()) ||
                    "inproceedings".equals(name.getLocalPart()) ||
                    "proceedings".equals(name.getLocalPart()) ||
                    "book".equals(name.getLocalPart()) ||
                    "incollection".equals(name.getLocalPart()) ||
                    "phdthesis".equals(name.getLocalPart()) ||
                    "mastersthesis".equals(name.getLocalPart()) ||
                    "www".equals(name.getLocalPart());
        }

        @Override
        public boolean skip(QName name) {
            return false;
        }

        @Override
        public void identify(QName name, String value, IRI identifier) {

        }

        @Override
        public void closeResource() throws IOException {
            super.closeResource();
            if (settings.get("collection") != null) {
                getResource().add("collection", settings.get("collection"));
            }
            params.setHandler((content, p) -> {
                if (ingest.client() != null) {
                    IndexRequestBuilder indexRequestBuilder = new IndexRequestBuilder(ingest.client(), IndexAction.INSTANCE)
                            .setIndex(p.getIndex())
                            .setType(p.getType())
                            .setId(id)
                            .setSource(content);
                    ingest.bulkIndex(indexRequestBuilder.request());
                }
            });
            RdfContentBuilder builder = routeRdfXContentBuilder(params);
            builder.receive(getResource());
            if (settings.getAsBoolean("mock", false)) {
                logger.info("{}", builder.string());
            }
        }

        @Override
        public XmlHandler setNamespaceContext(IRINamespaceContext namespaceContext) {
            return this;
        }

        @Override
        public IRINamespaceContext getNamespaceContext() {
            return namespaceContext;
        }
    }

}

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
package org.xbib.tools.feed.elasticsearch.medline;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.common.xcontent.XContentHelper;
import org.xbib.grouping.bibliographic.endeavor.WorkAuthor;
import org.xbib.tools.convert.Converter;
import org.xbib.util.InputService;
import org.xbib.iri.IRI;
import org.xbib.iri.namespace.IRINamespaceContext;
import org.xbib.rdf.RdfConstants;
import org.xbib.rdf.RdfContentBuilder;
import org.xbib.rdf.RdfContentParams;
import org.xbib.rdf.content.RdfXContentParams;
import org.xbib.rdf.content.RouteRdfXContentParams;
import org.xbib.rdf.io.xml.XmlContentParser;
import org.xbib.rdf.io.xml.AbstractXmlHandler;
import org.xbib.rdf.io.xml.AbstractXmlResourceHandler;
import org.xbib.rdf.io.xml.XmlHandler;
import org.xbib.tools.feed.elasticsearch.Feeder;
import org.xbib.util.concurrent.WorkerProvider;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.xbib.rdf.content.RdfXContentFactory.rdfXContentBuilder;
import static org.xbib.rdf.content.RdfXContentFactory.routeRdfXContentBuilder;

/**
 * Elasticsearch indexer tool for Medline XML files
 */
public final class Medline extends Feeder {

    private final static Logger logger = LogManager.getLogger(Medline.class.getSimpleName());

    private final IRINamespaceContext namespaceContext = IRINamespaceContext.newInstance();

    @Override
    protected WorkerProvider<Converter> provider() {
        return p -> new Medline().setPipeline(p);
    }

    @Override
    public void process(URI uri) throws Exception {
        logger.debug("start uri={}", uri);
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
        AbstractXmlHandler handler = new Handler(params)
                .setDefaultNamespace("ml", "http://www.nlm.nih.gov/medline");
        InputStream in = InputService.getInputStream(uri);
        new XmlContentParser(in).setNamespaces(false)
                .setHandler(handler)
                .parse();
        in.close();
        logger.debug("end uri={}", uri);
    }

    private class Handler extends AbstractXmlResourceHandler {

        private String id;

        private List<String> author = new LinkedList<>();

        private String work;

        private String forename;

        private String lastname;

        private String date;

        private String volume;

        private String issue;

        public Handler(RdfContentParams params) {
            super(params);
        }

        @Override
        public void closeResource() throws IOException {
            super.closeResource();
            // create bibliographic key
            // there are works with "no authors listed" (e.g. PMID 5236443)
            if (work != null) {
                String key = new WorkAuthor()
                        .authorName(author)
                        .workName(work)
                        .chronology(date)
                        .chronology(volume)
                        .chronology(issue)
                        .createIdentifier();
                getResource().add("xbib:key", key);
            }
            RouteRdfXContentParams params = new RouteRdfXContentParams(getNamespaceContext(),
                    settings.get("index", "medline"),
                    settings.get("type", "medline"));
            params.setHandler((content, p) -> ingest.index(p.getIndex(), p.getType(), id, convert2fabio(content)));
            RdfContentBuilder builder = routeRdfXContentBuilder(params);
            builder.receive(getResource());
            if (settings.getAsBoolean("mock", false)) {
                logger.info("result = {}", params.getGenerator().get());
            }
            id = null;
            author.clear();
            work = null;
            date = null;
            volume = null;
            issue = null;
        }

        private String convert2fabio(String content) {
            Map<String,Object> map = XContentHelper.convertToMap(content);
            MedlineMapper mf = new MedlineMapper();
            RdfContentBuilder builder;
            try {
                RdfXContentParams params = new RdfXContentParams(namespaceContext);
                builder = rdfXContentBuilder(params);
                builder.receive(mf.map(map));
                return params.getGenerator().get();
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
            return null;
        }

        @Override
        public boolean isResourceDelimiter(QName name) {
            return "MedlineCitation".equals(name.getLocalPart());
        }

        @Override
        public void identify(QName name, String value, IRI identifier) {
            // important: there are many occurances of PMID.
            // We must only take the first occurance for the ID.
            if (id == null && "PMID".equals(name.getLocalPart())) {
                this.id = value;
            } else if ("ArticleTitle".equals(name.getLocalPart())) {
                this.work = value;
            } else if ("LastName".equals(name.getLocalPart())) {
                this.lastname = value;
            } else if ("ForeName".equals(name.getLocalPart())) {
                this.forename = value;
                if (forename != null && lastname != null) {
                    author.add(lastname + " " + forename);
                    forename = null;
                    lastname = null;
                }
            } else if ("Year".equals(name.getLocalPart())
                    && ("PubDate".equals(parents.peek().getLocalPart()) || "DateCreated".equals(parents.peek().getLocalPart()))) {
                date = value;
            } else if ("Volume".equals(name.getLocalPart())) {
                volume = value;
            } else if ("Issue".equals(name.getLocalPart())) {
                // issue is needed, see e.g. PMID 5015805
                issue = value;
            }
        }

        @Override
        public boolean skip(QName name) {
            boolean isAttr = name.getLocalPart().startsWith("@");
            return "MedlineCitationSet".equals(name.getLocalPart())
                    || "MedlineCitation".equals(name.getLocalPart())
                    || isAttr;
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

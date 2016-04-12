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
package org.xbib.oai.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.xbib.oai.client.listrecords.ListRecordsListener;
import org.xbib.rdf.RdfContentParams;
import org.xbib.rdf.memory.MemoryLiteral;
import org.xbib.iri.IRI;
import org.xbib.oai.client.listrecords.ListRecordsRequest;
import org.xbib.oai.rdf.RdfSimpleMetadataHandler;
import org.xbib.oai.rdf.RdfResourceHandler;
import org.xbib.iri.namespace.IRINamespaceContext;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.StringWriter;
import java.time.Instant;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * DOAJ client test
 *
 */
public class DOAJArticleClientTest {

    private final static Logger logger = LogManager.getLogger(DOAJArticleClientTest.class.getName());

    private final static String DOAJ_NS_PREFIX = "doaj";

    private final static String DOAJ_NS_URI = "http://www.doaj.org/schemas/";


    public void testListRecordsDOAJArticles() {

        IRINamespaceContext namespaceContext = RdfSimpleMetadataHandler.getDefaultContext();
        namespaceContext.addNamespace(DOAJ_NS_PREFIX, DOAJ_NS_URI);

        RdfContentParams params = () -> namespaceContext;
        final RdfSimpleMetadataHandler metadataHandler = new RdfSimpleMetadataHandler(params);
        final RdfResourceHandler resourceHandler = new DOAJResourceHandler(params);
        resourceHandler.setDefaultNamespace(DOAJ_NS_PREFIX,  DOAJ_NS_URI);

        metadataHandler.setHandler(resourceHandler);

        try {
            OAIClient client = OAIClientFactory.newClient("http://doaj.org/oai.article");
            ListRecordsRequest request = client.newListRecordsRequest()
                    .setFrom(Instant.parse("2016-01-16T00:00:00Z"))
                    .setUntil(Instant.parse("2016-01-17T00:00:00Z"))
                    .setMetadataPrefix("oai_dc"); // doajArticle format no longer there!

            do {
                ListRecordsListener listener = new ListRecordsListener(request);
                request.addHandler(metadataHandler);
                request.prepare().execute(listener).waitFor();
                if (listener.getResponse() != null) {
                    StringWriter sw = new StringWriter();
                    listener.getResponse().to(sw);
                    logger.info("response = {}", sw);
                }
                request = client.resume(request, listener.getResumptionToken());
            } while (request != null);

            client.close();
        } catch (IOException | InterruptedException | TimeoutException | ExecutionException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private final IRI ISSN = IRI.create("urn:ISSN");

    private final IRI EISSN = IRI.create("urn:EISSN");

    class DOAJResourceHandler extends RdfResourceHandler {

        public DOAJResourceHandler(RdfContentParams params) {
            super(params);
        }

        @Override
        public boolean isResourceDelimiter(QName name) {
            boolean b = DOAJ_NS_URI.equals(name.getNamespaceURI())
                    && "record".equals(name.getLocalPart());
            return b;
        }

        @Override
        public boolean skip(QName name) {
            boolean b = OAIDC_NS_URI.equals(name.getNamespaceURI())
                    && DC_PREFIX.equals(name.getLocalPart());
            b = b || "record".equals(name.getLocalPart());
            return b;
        }

        @Override
        public IRI toProperty(IRI property) {
            if ("issn".equals(property.getSchemeSpecificPart())) {
                return IRI.builder().curie("dc", "identifier").build();
            }
            if ("eissn".equals(property.getSchemeSpecificPart())) {
                return IRI.builder().curie("dc", "identifier").build();
            }
            return property;
        }

        @Override
        public Object toObject(QName name, String content) {
            if ("issn".equals(name.getLocalPart())) {
                return new MemoryLiteral(content.substring(0,4) + "-" + content.substring(4)).type(ISSN);
            }
            if ("eissn".equals(name.getLocalPart())) {
                return new MemoryLiteral(content.substring(0,4) + "-" + content.substring(4)).type(EISSN);
            }
            return content;
        }
    }
}

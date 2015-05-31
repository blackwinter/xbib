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
import org.testng.annotations.Test;
import org.xbib.iri.namespace.IRINamespaceContext;
import org.xbib.oai.OAIDateResolution;
import org.xbib.oai.client.listrecords.ListRecordsListener;
import org.xbib.rdf.RdfContentParams;
import org.xbib.rdf.memory.MemoryLiteral;
import org.xbib.util.DateUtil;
import org.xbib.iri.IRI;
import org.xbib.oai.client.listrecords.ListRecordsRequest;
import org.xbib.oai.rdf.RdfSimpleMetadataHandler;
import org.xbib.oai.rdf.RdfResourceHandler;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.StringWriter;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.testng.Assert.assertTrue;

/**
 * DOAJ client test
 */
public class DOAJClientTest {

    private final static Logger logger = LogManager.getLogger(DOAJClientTest.class);

    @Test
    public void testListRecordsDOAJ() {

        RdfContentParams params = IRINamespaceContext::newInstance;

        final RdfSimpleMetadataHandler metadataHandler = new RdfSimpleMetadataHandler(params);
        final RdfResourceHandler resourceHandler = new DOAJResourceHandler(params);

        metadataHandler.setHandler(resourceHandler);

        int count = 0;
        try {
            OAIClient client = OAIClientFactory.newClient("DOAJ");
            ListRecordsRequest request = client.newListRecordsRequest()
                    .setFrom( DateUtil.parseDateISO("2015-04-01T00:00:00Z"), OAIDateResolution.DAY)
                    .setUntil(DateUtil.parseDateISO("2015-05-01T00:00:00Z"), OAIDateResolution.DAY)
                    .setMetadataPrefix("oai_dc");
            do {
                ListRecordsListener listener = new ListRecordsListener(request);
                request.addHandler(metadataHandler);
                request.prepare().execute(listener).waitFor();
                if (listener.getResponse() != null) {
                    StringWriter sw = new StringWriter();
                    listener.getResponse().to(sw);
                    logger.debug("response  = {}", sw);
                    count++;
                }
                request = client.resume(request, listener.getResumptionToken());
            } while (request != null);
            client.close();
        } catch (IOException | InterruptedException | TimeoutException | ExecutionException e) {
            logger.error(e.getMessage(), e);
        }
        assertTrue(count > 0);
    }

    private final IRI ISSN = IRI.create("urn:ISSN");
    private final IRI EISSN = IRI.create("urn:EISSN");
    private final IRI LCCN = IRI.create("urn:LCC");

    class DOAJResourceHandler extends RdfResourceHandler {

        public DOAJResourceHandler(RdfContentParams params) {
            super(params);
        }

        @Override
        public Object toObject(QName name, String content) {
            String s = name.getLocalPart();
            if (s.equals("identifier")) {
                if (content.startsWith("http://")) {
                    return new MemoryLiteral(content).type(IRI.create("xsd:anyUri"));
                }
                if (content.startsWith("issn: ")) {
                    return new MemoryLiteral(content.substring(6)).type(ISSN);
                }
                if (content.startsWith("eissn: ")) {
                    return new MemoryLiteral(content.substring(7)).type(EISSN);
                }
            } else if (s.equals("subject")) {
                if (content.startsWith("LCC: ")) {
                    return new MemoryLiteral(content.substring(5)).type(LCCN);
                }
            }
            return content;
        }
    }
}

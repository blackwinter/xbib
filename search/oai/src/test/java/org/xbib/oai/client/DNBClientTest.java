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

import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

import org.testng.annotations.Test;
import org.xbib.oai.listrecords.ListRecordsListener;
import org.xbib.util.DateUtil;
import org.xbib.iri.IRI;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.oai.identify.IdentifyRequest;
import org.xbib.oai.identify.IdentifyResponseListener;
import org.xbib.oai.listrecords.ListRecordsRequest;
import org.xbib.oai.xml.MetadataHandler;
import org.xbib.rdf.Triple;
import org.xbib.rdf.io.TripleListener;
import org.xbib.rdf.io.xml.XmlReader;
import org.xbib.rdf.io.xml.XmlTriplifier;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class DNBClientTest {

    private final Logger logger = LoggerFactory.getLogger(DNBClientTest.class.getName());

    @Test
    public void testIdentify() throws Exception {
        OAIClient client = OAIClientFactory.newClient("http://services.dnb.de/oai/repository");
        IdentifyRequest request = client.newIdentifyRequest();
        request.prepare().execute(new IdentifyResponseListener() {}).waitFor();
    }

    @Test
    public void testListRecordsDNB() throws Exception {

        OAIClient client = OAIClientFactory.newClient("http://services.dnb.de/oai/repository");
        ListRecordsRequest request =  client.newListRecordsRequest()
                .setFrom(DateUtil.parseDateISO("2013-01-01T00:00:00Z"))
                .setUntil(DateUtil.parseDateISO("2013-01-10T00:00:00Z"))
                .setSet("bib")
                .setMetadataPrefix("PicaPlus-xml");
        final XmlTriplifier reader = new XmlReader();
        final AtomicLong count = new AtomicLong(0L);
        MetadataHandler metadataHandler = new MetadataHandler() {
            @Override
            public void startDocument() throws SAXException {
                logger.debug("startDocument");
            }

            @Override
            public void endDocument() throws SAXException {
                count.incrementAndGet();
                logger.debug("endDocument");
            }

            @Override
            public void startPrefixMapping(String prefix, String uri) throws SAXException {
            }

            @Override
            public void endPrefixMapping(String prefix) throws SAXException {
            }

            @Override
            public void startElement(String ns, String localname, String qname, Attributes atrbts) throws SAXException {
            }

            @Override
            public void endElement(String ns, String localname, String qname) throws SAXException {
            }

            @Override
            public void characters(char[] chars, int pos, int len) throws SAXException {
            }

        };
        FileWriter sw = new FileWriter("target/dnb-bib-pica.xml");
        do {
            try {
                request.addHandler(metadataHandler);
                ListRecordsListener listener = new ListRecordsListener(request);                        ;
                request.prepare().execute(listener).waitFor();
                if (listener.getResponse() != null) {
                    listener.getResponse().to(sw);
                } else {
                    logger.warn("no response in listener");
                }
                request = client.resume(request, listener.getResumptionToken());
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
                request  = null;
            }
        } while (request != null);
        sw.close();
        client.close();

        logger.info("count={}", count.get());
    }

}

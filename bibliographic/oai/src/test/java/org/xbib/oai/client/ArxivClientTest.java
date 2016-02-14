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
import org.xbib.oai.client.identify.IdentifyRequest;
import org.xbib.oai.client.identify.IdentifyResponseListener;
import org.xbib.oai.client.listrecords.ListRecordsListener;
import org.xbib.oai.client.listrecords.ListRecordsRequest;
import org.xbib.oai.xml.SimpleMetadataHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ConnectException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

public class ArxivClientTest {

    private final static Logger logger = LogManager.getLogger(ArxivClientTest.class.getName());

    @Test
    public void testListRecordsArxiv() throws InterruptedException, TimeoutException, IOException {
        try {
            OAIClient client = OAIClientFactory.newClient("http://export.arxiv.org/oai2");
            IdentifyRequest identifyRequest = client.newIdentifyRequest();
            IdentifyResponseListener identifyResponseListener = new IdentifyResponseListener(identifyRequest);
            identifyRequest.prepare().execute(identifyResponseListener).waitFor();
            String granularity = identifyResponseListener.getResponse().getGranularity();
            logger.info("granularity = {}", granularity);
            DateTimeFormatter dateTimeFormatter = "YYYY-MM-DD".equals(granularity) ?
                    DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.of("GMT")) : null;
            // ArXiv wants us to wait 20 secs between *every* HTTP request, so we muste wait here
            Thread.sleep(20 * 1000L);
            ListRecordsRequest request = client.newListRecordsRequest()
                    .setDateTimeFormatter(dateTimeFormatter)
                    .setFrom(Instant.parse("2013-02-01T00:00:00Z"))
                    .setUntil(Instant.parse("2013-02-02T00:00:00Z"))
                    .setMetadataPrefix("arXiv");
            final AtomicLong count = new AtomicLong(0L);
            SimpleMetadataHandler simpleMetadataHandler = new SimpleMetadataHandler() {
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
            File file = File.createTempFile("arxiv.", ".xml");
            FileWriter sw = new FileWriter(file);
            do {
                try {
                    request.addHandler(simpleMetadataHandler);
                    ListRecordsListener listener = new ListRecordsListener(request);
                    request.prepare().execute(listener).waitFor();
                    if (listener.getResponse() != null) {
                        listener.getResponse().to(sw);
                    } else {
                        logger.warn("no response in listener");
                    }
                    request = client.resume(request, listener.getResumptionToken());
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                    request = null;
                }
            } while (request != null);
            sw.close();
            client.close();
            logger.info("count={}", count.get());
        } catch (ConnectException | ExecutionException e) {
            logger.warn("skipped, can not connect");
        } catch (TimeoutException | InterruptedException | IOException e) {
            throw e;
        }
    }

}

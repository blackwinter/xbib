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
import org.xbib.oai.client.listrecords.ListRecordsResponse;
import org.xbib.oai.xml.SimpleMetadataHandler;
import org.xbib.service.client.http.SimpleHttpResponse;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ConnectException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertTrue;

public class ArxivClientTest {

    private final static Logger logger = LogManager.getLogger(ArxivClientTest.class.getName());

    @Test
    public void testListRecordsArxiv() throws InterruptedException, TimeoutException, IOException {
        try {
            OAIClient client = OAIClientFactory.newClient("http://export.arxiv.org/oai2");
            IdentifyRequest identifyRequest = client.newIdentifyRequest();
            SimpleHttpResponse simpleHttpResponse = client.getHttpClient().execute(identifyRequest.getHttpRequest()).get();
            IdentifyResponseListener identifyResponseListener = new IdentifyResponseListener(identifyRequest);
            String content = new String(simpleHttpResponse.content(), StandardCharsets.UTF_8);
            logger.debug("identifyResponse = {}", content);
            identifyResponseListener.onReceive(content);
            identifyResponseListener.receivedResponse();
            String granularity = identifyResponseListener.getResponse().getGranularity();
            logger.info("granularity = {}", granularity);
            DateTimeFormatter dateTimeFormatter = "YYYY-MM-DD".equals(granularity) ?
                    DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.of("GMT")) : null;
            // ArXiv wants us to wait 20 secs between *every* HTTP request, so we must wait here
            Thread.sleep(20 * 1000L);
            ListRecordsRequest listRecordsRequest = client.newListRecordsRequest()
                    .setDateTimeFormatter(dateTimeFormatter)
                    .setFrom(Instant.parse("2016-05-01T00:00:00Z"))
                    .setUntil(Instant.parse("2016-05-02T00:00:00Z"))
                    .setMetadataPrefix("arXiv");
            final AtomicLong count = new AtomicLong(0L);
            SimpleMetadataHandler simpleMetadataHandler = new SimpleMetadataHandler() {
                @Override
                public void startDocument() throws SAXException {
                    logger.debug("start doc");
                }

                @Override
                public void endDocument() throws SAXException {
                    logger.debug("end doc");
                    count.incrementAndGet();
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
            file.deleteOnExit();
            FileWriter fileWriter = new FileWriter(file);
            do {
                try {
                    listRecordsRequest.addHandler(simpleMetadataHandler);
                    simpleHttpResponse = client.getHttpClient().execute(listRecordsRequest.getHttpRequest()).get();
                    logger.debug("response headers = {}", simpleHttpResponse.headers().entries());
                    ListRecordsListener listener = new ListRecordsListener(listRecordsRequest);
                    content = new String(simpleHttpResponse.content(), StandardCharsets.UTF_8);
                    listener.onReceive(content);
                    listener.receivedResponse(simpleHttpResponse);
                    if (listener.getResponse() != null) {
                        listener.getResponse().to(fileWriter);
                        logger.info("delay={} resumption token={}",
                                listener.getResponse().getDelaySeconds(), listener.getResumptionToken());
                    } else {
                        logger.warn("no response in listener");
                    }
                    listRecordsRequest = client.resume(listRecordsRequest, listener.getResumptionToken());
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                    listRecordsRequest = null;
                }
            } while (listRecordsRequest != null);
            fileWriter.close();
            client.close();
            logger.info("count={}", count.get());
            assertTrue(count.get() > 0L);
        } catch (ConnectException | ExecutionException e) {
            logger.warn("skipped, can not connect", e);
        } catch (InterruptedException | IOException e) {
            throw e;
        }
    }

}

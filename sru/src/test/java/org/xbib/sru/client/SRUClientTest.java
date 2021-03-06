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
package org.xbib.sru.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.xbib.marc.MarcXchangeStream;
import org.xbib.marc.xml.sax.MarcXchangeContentHandler;
import org.xbib.sru.searchretrieve.SearchRetrieveListener;
import org.xbib.sru.searchretrieve.SearchRetrieveRequest;
import org.xbib.sru.searchretrieve.SearchRetrieveResponse;
import org.xbib.sru.searchretrieve.SearchRetrieveResponseAdapter;
import org.xbib.xml.stream.SaxEventConsumer;
import org.xbib.xml.transform.StylesheetTransformer;

import javax.xml.stream.util.XMLEventConsumer;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.Normalizer;
import java.util.Arrays;

public class SRUClientTest {

    private final static Logger logger = LogManager.getLogger(SRUClientTest.class);

    @Test
    public void testClient() throws Exception {
        try {
            SRUClient<SearchRetrieveRequest,SearchRetrieveResponse> client = new DefaultSRUClient<>();
            SearchRetrieveRequest request = client
                    .newSearchRetrieveRequest("h1://pub.uni-bielefeld.de/sru")
                    .setQuery("title=linux")
                    .setStartRecord(0)
                    .setMaximumRecords(10);

            File file = File.createTempFile("sru-client-bielefeld", ".xml");
            FileOutputStream out = new FileOutputStream(file);
            Writer writer = new OutputStreamWriter(out, "UTF-8");
            SearchRetrieveListener listener = new SearchRetrieveResponseAdapter() {

                @Override
                public void version(String version) {
                    //logger.info("version = " + version);
                }

                @Override
                public void numberOfRecords(long numberOfRecords) {
                    //logger.info("numberOfRecords = " + numberOfRecords);
                }

                @Override
                public void beginRecord() {
                    logger.info("begin record");
                }

                @Override
                public void recordSchema(String recordSchema) {
                    //logger.info("got record scheme:" + recordSchema);
                }

                @Override
                public void recordPacking(String recordPacking) {
                    //logger.info("got recordPacking: " + recordPacking);
                }

                @Override
                public void recordIdentifier(String recordIdentifier) {
                    //logger.info("got recordIdentifier=" + recordIdentifier);
                }

                @Override
                public void recordPosition(int recordPosition) {
                    //logger.info("got recordPosition=" + recordPosition);
                }

                @Override
                public XMLEventConsumer recordData() {
                    //logger.info("recordData = " + record.size() + " events");
                    return null;
                }

                @Override
                public XMLEventConsumer extraRecordData() {
                    //logger.info("extraRecordData = " + record.size() + " events");
                    return null;
                }

                @Override
                public void endRecord() {
                    logger.debug("end record");
                }

            };
            request.addListener(listener);
            StylesheetTransformer transformer = new StylesheetTransformer().setPath("src/test/resources/xsl");
            client.searchRetrieve(request)
                    .setStylesheetTransformer(transformer)
                    .to(writer);
            transformer.close();
            client.close();
            writer.close();
            out.close();
        } catch (Exception e) {
            // we tolerate failures
            //logger.warn(e.getMessage(), e);
        }
    }

    @Test
    public void testServiceSearchRetrieve() throws Exception {
        try {
            final MarcXchangeStream kv = new MarcXchangeStream()
                    .setStringTransformer(value -> Normalizer.normalize(value, Normalizer.Form.NFC));

            final MarcXchangeContentHandler marcXmlHandler = new MarcXchangeContentHandler()
                    .addListener("Bibliographic", kv);

            for (String clientName : Arrays.asList("h1://pub.uni-bielefeld.de/sru",
                    "http://biblio.ugent.be/sru",
                    "http://lup.lub.lu.se/sru"
            )) {
                String query = "title=linux";
                int from = 1;
                int size = 10;
                File file = File.createTempFile("sru-service-" + clientName + ".", ".xml");
                FileOutputStream out = new FileOutputStream(file);
                Writer w = new OutputStreamWriter(out, "UTF-8");
                SearchRetrieveListener listener = new SearchRetrieveResponseAdapter() {

                    @Override
                    public void version(String version) {
                        //logger.info("version = " + version);
                    }

                    @Override
                    public void numberOfRecords(long numberOfRecords) {
                        //logger.info("numberOfRecords = " + numberOfRecords);
                    }

                    @Override
                    public void beginRecord() {
                        logger.debug("begin record");
                    }

                    @Override
                    public void recordSchema(String recordSchema) {
                        //logger.info("got record scheme:" + recordSchema);
                    }

                    @Override
                    public void recordPacking(String recordPacking) {
                        //logger.info("got recordPacking: " + recordPacking);
                    }

                    @Override
                    public void recordIdentifier(String recordIdentifier) {
                        //logger.info("got recordIdentifier=" + recordIdentifier);
                    }

                    @Override
                    public void recordPosition(int recordPosition) {
                        //logger.info("got recordPosition=" + recordPosition);
                    }

                    @Override
                    public XMLEventConsumer recordData() {
                        return new SaxEventConsumer(marcXmlHandler);
                    }

                    @Override
                    public XMLEventConsumer extraRecordData() {
                        return null;
                    }

                    @Override
                    public void endRecord() {
                        logger.debug("end record");
                    }

                };
                SRUClient<SearchRetrieveRequest, SearchRetrieveResponse> client = new DefaultSRUClient<>();
                SearchRetrieveRequest request = client.newSearchRetrieveRequest(clientName)
                        .addListener(listener)
                        .setQuery(query)
                        .setStartRecord(from)
                        .setMaximumRecords(size);
                SearchRetrieveResponse response = client.searchRetrieve(request).to(w);
                logger.info("http status = {}", response.getSimpleHttpResponse().status());
                client.close();
                w.close();
                out.close();
            }
        } catch (Exception e) {
            // we tolerate failures but log them
            logger.warn(e.getMessage(), e);
        }
    }
}

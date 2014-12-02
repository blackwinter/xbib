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

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.Normalizer;
import java.util.Arrays;
import javax.xml.stream.util.XMLEventConsumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.Test;
import org.xbib.io.Request;
import org.xbib.keyvalue.KeyValueStreamAdapter;
import org.xbib.marc.FieldList;
import org.xbib.marc.Field;
import org.xbib.marc.keyvalue.MarcXchange2KeyValue;
import org.xbib.marc.transformer.StringTransformer;
import org.xbib.marc.xml.MarcXchangeContentHandler;
import org.xbib.sru.searchretrieve.SearchRetrieveListener;
import org.xbib.sru.searchretrieve.SearchRetrieveRequest;
import org.xbib.sru.searchretrieve.SearchRetrieveResponseAdapter;
import org.xbib.sru.searchretrieve.SearchRetrieveResponse;
import org.xbib.xml.stream.SaxEventConsumer;

public class SRUClientTest {

    private static final Logger logger = LogManager.getLogger(SRUClientTest.class.getName());

    @Test
    public void testServiceSearchRetrieve() throws Exception {

        final MarcXchange2KeyValue kv = new MarcXchange2KeyValue()
                .setStringTransformer(new StringTransformer() {
                    @Override
                    public String transform(String value) {
                        return Normalizer.normalize(value, Normalizer.Form.NFC);
                    }
                })
                .addListener(new KeyValueStreamAdapter<FieldList, String>() {
                    @Override
                    public KeyValueStreamAdapter<FieldList, String> begin() {
                        logger.debug("begin object");
                        return this;
                    }

                    @Override
                    public KeyValueStreamAdapter<FieldList, String> keyValue(FieldList key, String value) {
                        logger.debug("begin");
                        for (Field f : key) {
                            logger.debug("tag={} ind={} subf={} data={}",
                                    f.tag(), f.indicator(), f.subfieldId(), f.data());
                        }
                        if (value != null) {
                            logger.debug("value={}", value);

                        }
                        logger.debug("end");
                        return this;
                    }

                    @Override
                    public KeyValueStreamAdapter<FieldList, String> end() {
                        logger.debug("end object");
                        return this;
                    }
                });

        final MarcXchangeContentHandler marcXmlHandler = new MarcXchangeContentHandler()
                .addListener("Bibliographic", kv);

        for (String clientName : Arrays.asList("Gent", "Lund", "Bielefeld")) {
            String query = "title=linux";
            int from = 1;
            int size = 10;
            File file = File.createTempFile("sru-service-" +clientName + ".",".xml");
            FileOutputStream out = new FileOutputStream(file);
            Writer w = new OutputStreamWriter(out, "UTF-8");
            SearchRetrieveListener listener = new SearchRetrieveResponseAdapter() {

                @Override
                public void onConnect(Request request) {
                    logger.info("connect, request = " + request);
                }

                @Override
                public void version(String version) {
                    logger.info("version = " + version);
                }

                @Override
                public void numberOfRecords(long numberOfRecords) {
                    logger.info("numberOfRecords = " + numberOfRecords);
                }

                @Override
                public void beginRecord() {
                    logger.info("begin record");
                }

                @Override
                public void recordSchema(String recordSchema) {
                    logger.info("got record scheme:" + recordSchema);
                }

                @Override
                public void recordPacking(String recordPacking) {
                    logger.info("got recordPacking: " + recordPacking);
                }
                @Override
                public void recordIdentifier(String recordIdentifier) {
                    logger.info("got recordIdentifier=" + recordIdentifier);
                }

                @Override
                public void recordPosition(int recordPosition) {
                    logger.info("got recordPosition=" + recordPosition);
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
                    logger.info("end record");
                }

                @Override
                public void onDisconnect(Request request) {
                    logger.info("disconnect, request = " + request);
                }
            };
            SRUClient client = SRUClientFactory.newClient(clientName);
            SearchRetrieveRequest request = client.newSearchRetrieveRequest()
                    .addListener(listener)
                    .setQuery(query)
                    .setStartRecord(from)
                    .setMaximumRecords(size);
            SearchRetrieveResponse response = client.searchRetrieve(request).to(w);
            logger.info("http status = {}", response.httpStatus());
            client.close();
            w.close();
            out.close();
        }
    }
}

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

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.util.XMLEventConsumer;

import io.netty.channel.ConnectTimeoutException;
import org.testng.annotations.Test;
import org.xbib.io.Request;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.sru.SRUResponse;
import org.xbib.sru.searchretrieve.SearchRetrieveListener;
import org.xbib.sru.searchretrieve.SearchRetrieveRequest;
import org.xbib.sru.searchretrieve.SearchRetrieveResponseAdapter;
import org.xbib.xml.transform.StylesheetTransformer;

public class AsyncClientTest {

    private static final Logger logger = LoggerFactory.getLogger(AsyncClientTest.class.getName());

    @Test
    public void testMultiClient() throws Exception {
        for (String s : Arrays.asList("Bielefeld", "Gent", "Lund")) {
            SRUClient client = SRUClientFactory.newClient(s);
            String query = "title=linux";
            int from = 0;
            int size = 10;
            SearchRetrieveRequest request = client
                    .newSearchRetrieveRequest()
                    .setQuery(query)
                    .setStartRecord(from)
                    .setMaximumRecords(size);
            FileOutputStream out = new FileOutputStream("target/sru-async-" + request.getURI().getHost() + ".xml");
            Writer writer = new OutputStreamWriter(out, "UTF-8");

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
                    logger.info("end record");
                }

                @Override
                public void onDisconnect(Request request) {
                    logger.info("disconnect, request = " + request);
                }
            };
            request.addListener(listener);
            StylesheetTransformer transformer = new StylesheetTransformer("src/test/resources/xsl");
            try {
                SRUResponse response = client
                        .searchRetrieve(request)
                        .setStylesheetTransformer(transformer)
                        .to(writer);
            } catch (ConnectTimeoutException e) {
                logger.error(e.getMessage(), e);
            }
            transformer.close();
            client.close();
        }
    }
}

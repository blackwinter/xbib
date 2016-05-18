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
package org.xbib.sru.iso23950;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Collections;
import javax.xml.stream.util.XMLEventConsumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.junit.Test;
import org.xbib.io.iso23950.searchretrieve.ZSearchRetrieveRequest;
import org.xbib.io.iso23950.searchretrieve.ZSearchRetrieveResponse;
import org.xbib.sru.client.SRUClient;
import org.xbib.sru.iso23950.service.ZSRUServiceFactory;
import org.xbib.sru.searchretrieve.SearchRetrieveListener;
import org.xbib.sru.searchretrieve.SearchRetrieveResponseAdapter;
import org.xbib.sru.service.SRUService;
import org.xbib.xml.transform.StylesheetTransformer;

public class SRUServiceTest {

    private final static Logger logger = LogManager.getLogger(SRUServiceTest.class.getName());

    @Test
    public void testSearchRetrieve() throws Exception {
        for (String name : Collections.singletonList("OBVSG")) {
            logger.info("trying " + name);
            SRUService<ZSearchRetrieveRequest, ZSearchRetrieveResponse> service = ZSRUServiceFactory.getService(name);
            if (service != null) {
                File file = File.createTempFile("sru-" + service.getURI().getHost(), ".xml");
                FileOutputStream out = new FileOutputStream(file);
                Writer w = new OutputStreamWriter(out, "UTF-8");
                try {
                    SearchRetrieveListener listener = new SearchRetrieveResponseAdapter() {
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
                            logger.info("startStream record");
                        }

                        @Override
                        public void recordSchema(String recordSchema) {
                            logger.info("got recordSchema=" + recordSchema);
                        }

                        @Override
                        public void recordPacking(String recordPacking) {
                            logger.info("got recordPacking=" + recordPacking);
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
                            return null;
                        }

                        @Override
                        public XMLEventConsumer extraRecordData() {
                            return null;
                        }

                        @Override
                        public void endRecord() {
                            logger.info("endStream record");
                        }

                    };
                    String query = "dc.title = Linux";
                    int from = 1;
                    int size = 10;
                    SRUClient<ZSearchRetrieveRequest, ZSearchRetrieveResponse> client = service.newClient();
                    ZSearchRetrieveRequest request = client.newSearchRetrieveRequest(service.getURI().toString());
                    request.addListener(listener)
                            .setQuery(query)
                            .setStartRecord(from)
                            .setMaximumRecords(size);
                    StylesheetTransformer transformer = new StylesheetTransformer().setPath("src/main/resources/xsl");
                    client.searchRetrieve(request)
                            .setStylesheetTransformer(transformer)
                            .to(w);
                    transformer.close();
                    client.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
                w.close();
                out.close();
            }
        }
    }
}

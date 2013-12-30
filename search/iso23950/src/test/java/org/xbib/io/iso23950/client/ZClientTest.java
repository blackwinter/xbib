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
package org.xbib.io.iso23950.client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import org.testng.annotations.Test;
import org.xbib.io.iso23950.Diagnostics;
import org.xbib.io.iso23950.searchretrieve.ZSearchRetrieveRequest;
import org.xbib.io.iso23950.searchretrieve.ZSearchRetrieveResponse;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.xml.transform.StylesheetTransformer;

public class ZClientTest {

    private final Logger logger = LoggerFactory.getLogger(ZClientTest.class.getName());
    
    @Test
    public void testClient() {
        for (String serviceName : Arrays.asList("LIBRIS", "LOC", "OBVSG")) {
            try {
                logger.info("trying " + serviceName);
                String query = "dc.title = Linux";
                int from = 1;
                int size = 10;
                ZClient client = ZClientFactory.newZClient(serviceName);
                try {
                    File tmp = File.createTempFile(serviceName, "xml");
                    FileOutputStream out = new FileOutputStream(tmp);
                    try (Writer w = new OutputStreamWriter(out, "UTF-8")) {
                        ZSearchRetrieveRequest request = client.newCQLSearchRetrieveRequest()
                                .setQuery(query)
                                .setFrom(from)
                                .setSize(size);
                        ZSearchRetrieveResponse response = request.execute();
                        StylesheetTransformer transformer = new StylesheetTransformer("src/main/resources/xsl");
                        response.setStylesheetTransformer(transformer)
                                .to(w);
                    }
                    tmp.delete();
                } finally {
                    client.close();
                }
            } catch (Diagnostics d) {
                d.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

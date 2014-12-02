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
package org.xbib.io.iso23950.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.Test;
import org.xbib.io.iso23950.client.ZClient;
import org.xbib.io.iso23950.searchretrieve.ZSearchRetrieveRequest;
import org.xbib.xml.transform.StylesheetTransformer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;

public class ZServiceTest {

    private final static Logger logger = LogManager.getLogger(ZServiceTest.class.getName());

    @Test
    public void testService() {
        try {
            for (String serviceName : Arrays.asList("LIBRIS", "LOC", "OBVSG")) {
                ZService service = ZServiceFactory.getService(serviceName);
                ZClient client = service.newZClient();
                File file = File.createTempFile("service-" + service.getURI().getHost(), ".xml");
                FileOutputStream out = new FileOutputStream(file);
                Writer sw = new OutputStreamWriter(out, "UTF-8");
                String query = "@attr 1=4 test";
                int from = 1;
                int size = 10;
                try (StylesheetTransformer transformer = new StylesheetTransformer("src/main/resources")) {
                    ZSearchRetrieveRequest request = client.newPQFSearchRetrieveRequest();
                    request.setQuery(query)
                            .setFrom(from)
                            .setSize(size)
                            .execute()
                            .setStylesheetTransformer(transformer)
                            .to(sw);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                } finally {
                    service.close(client);
                }
                sw.close();
                out.close();
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }
}

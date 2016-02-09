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

import java.io.IOException;
import java.io.StringWriter;
import java.net.ConnectException;
import java.time.Instant;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.xbib.oai.client.listrecords.ListRecordsListener;
import org.xbib.oai.client.listrecords.ListRecordsRequest;

public class DNBOAITest {

    private final static Logger logger = LogManager.getLogger(DNBOAITest.class.getName());

    @Test
    public void testDNBOAI() throws InterruptedException, IOException, TimeoutException {
        try {
            OAIClient client = OAIClientFactory.newClient("DNB");
            ListRecordsRequest request = client.newListRecordsRequest()
                    .setMetadataPrefix("RDFxml")
                    .setSet("authorities")
                    .setFrom(Instant.parse("2012-01-23T00:00:00Z"))
                    .setUntil(Instant.parse("2012-01-23T01:00:00Z"));
            do {
                StringWriter sw = new StringWriter();
                ListRecordsListener listener = new ListRecordsListener(request);
                try {
                    request.prepare().execute(listener).waitFor();
                    if (listener.getResponse() != null) {
                        listener.getResponse().to(sw);
                        logger.info("response = {}", sw);
                    }
                    request = client.resume(request, listener.getResumptionToken());
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                    request = null;
                }

            } while (request != null);
            client.close();
        } catch (ConnectException | ExecutionException e) {
            logger.warn("skipped, can not connect");
        } catch (TimeoutException | InterruptedException | IOException e) {
            throw e;
        }
    }
}

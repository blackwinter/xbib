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
package org.xbib.sru.iso23950.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.io.iso23950.ZConnection;
import org.xbib.io.iso23950.ZSession;
import org.xbib.io.iso23950.client.ZClient;
import org.xbib.io.iso23950.searchretrieve.ZSearchRetrieveRequest;
import org.xbib.io.iso23950.searchretrieve.ZSearchRetrieveResponse;
import org.xbib.sru.client.DefaultSRUClient;
import org.xbib.sru.iso23950.service.ZSRUService;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 *  A SRU client for Z services
 */
public class ZSRUClient extends DefaultSRUClient<ZSearchRetrieveRequest, ZSearchRetrieveResponse> {

    private final static Logger logger = LogManager.getLogger(ZSRUClient.class);

    private final ZSRUService service;

    private ZClient client;

    private ZSession session;

    private ZConnection connection;

    ZSRUClient(ZSRUService service) throws IOException {
        super();
        this.service = service;
    }

    @Override
    public ZSearchRetrieveRequest newSearchRetrieveRequest(String url) {
        try {
            connection = new ZConnection(new URL(url));
            session = connection.createSession();
            client = session.newZClient();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return client.newCQLSearchRetrieveRequest();
    }

    @Override
    public ZSearchRetrieveResponse searchRetrieve(ZSearchRetrieveRequest request)
            throws IOException, InterruptedException, ExecutionException, TimeoutException {
        try {
            request.setDatabase(service.getDatabase())
                    .setResultSetName(service.getResultSetName())
                    .setElementSetName(service.getElementSetName())
                    .setPreferredRecordSyntax(service.getPreferredRecordSyntax())
                    .setQuery(request.getQuery())
                    .setFrom(request.getStartRecord())
                    .setSize(request.getMaximumRecords());
            return request.execute();
        } finally {
            if (client != null) {
                client.close();
            }
            if (session != null) {
                session.close();
            }
            if (connection != null) {
                connection.close();
            }
        }
    }

}

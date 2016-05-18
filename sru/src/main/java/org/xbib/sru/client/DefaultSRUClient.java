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

import io.netty.handler.codec.http.HttpHeaderNames;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.query.cql.SyntaxException;
import org.xbib.service.client.Clients;
import org.xbib.service.client.http.SimpleHttpClient;
import org.xbib.service.client.http.SimpleHttpRequest;
import org.xbib.service.client.http.SimpleHttpRequestBuilder;
import org.xbib.service.client.http.SimpleHttpResponse;
import org.xbib.service.client.invocation.RemoteInvokerFactory;
import org.xbib.sru.SRUConstants;
import org.xbib.sru.searchretrieve.SearchRetrieveRequest;
import org.xbib.sru.searchretrieve.SearchRetrieveResponse;
import org.xbib.util.URIBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * A default SRU client
 */
public class DefaultSRUClient<Request extends SearchRetrieveRequest, Response extends SearchRetrieveResponse>
        implements SRUClient<Request, Response> {

    private final static Logger logger = LogManager.getLogger(DefaultSRUClient.class);

    private final static RemoteInvokerFactory remoteInvokerFactory = RemoteInvokerFactory.DEFAULT;

    private SimpleHttpClient client;

    private String url;

    public DefaultSRUClient() throws IOException {
    }

    @Override
    public Request newSearchRetrieveRequest(String url) {
        this.url = url;
        this.client = Clients.newClient(remoteInvokerFactory, "none+" + url, SimpleHttpClient.class);
        SearchRetrieveRequest request = new ClientSearchRetrieveRequest();
        request.setQuery(URI.create(url).getQuery());
        return (Request) request;
    }

    @Override
    public Response searchRetrieve(Request request)
            throws SyntaxException, IOException, InterruptedException, ExecutionException, TimeoutException {
        if (request == null) {
            throw new IOException("request not set");
        }
        URIBuilder uriBuilder = new URIBuilder();
        URI uri = URI.create(url);
        uriBuilder.scheme(uri.getScheme())
                .authority(uri.getAuthority())
                .path(uri.getPath())
                .addParameter(SRUConstants.OPERATION_PARAMETER, "searchRetrieve")
                .addParameter(SRUConstants.VERSION_PARAMETER, request.getVersion())
                .addParameter(SRUConstants.QUERY_PARAMETER, request.getQuery())
                .addParameter(SRUConstants.START_RECORD_PARAMETER, Integer.toString(request.getStartRecord()))
                .addParameter(SRUConstants.MAXIMUM_RECORDS_PARAMETER, Integer.toString(request.getMaximumRecords()));
        if (request.getRecordPacking() != null && !request.getRecordPacking().isEmpty()) {
            uriBuilder.addParameter(SRUConstants.RECORD_PACKING_PARAMETER, request.getRecordPacking());
        }
        if (request.getRecordSchema() != null && !request.getRecordSchema().isEmpty()) {
            uriBuilder.addParameter(SRUConstants.RECORD_SCHEMA_PARAMETER, request.getRecordSchema());
        }
        SimpleHttpRequest simpleHttpRequest = SimpleHttpRequestBuilder.forGet(uriBuilder.build().toString()).build();
        SimpleHttpResponse simpleHttpResponse = client.execute(simpleHttpRequest).get();
        int max = 3;
        while (simpleHttpResponse.followUrl() != null && max-- > 0) {
            client = Clients.newClient(remoteInvokerFactory, "none+" + simpleHttpResponse.followUrl(),
                    SimpleHttpClient.class);
            simpleHttpRequest = SimpleHttpRequestBuilder.forGet(simpleHttpResponse.followUrl())
                    .header(HttpHeaderNames.ACCEPT, "utf-8")
                    .build();
            simpleHttpResponse = client.execute(simpleHttpRequest).get();
        }
        String content = new String(simpleHttpResponse.content(), StandardCharsets.UTF_8);
        logger.debug("content={}", content);
        final SearchRetrieveResponse response = new SearchRetrieveResponse(request, simpleHttpResponse);
        response.onReceive(content);
        return (Response) response;
    }

    @Override
    public void close() throws IOException {
    }

    private class ClientSearchRetrieveRequest extends SearchRetrieveRequest {
    }

}



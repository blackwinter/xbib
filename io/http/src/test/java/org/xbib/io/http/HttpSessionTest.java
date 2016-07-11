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
package org.xbib.io.http;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.Test;
import org.xbib.service.client.Clients;
import org.xbib.service.client.http.SimpleHttpClient;
import org.xbib.service.client.http.SimpleHttpRequest;
import org.xbib.service.client.http.SimpleHttpRequestBuilder;
import org.xbib.service.client.http.SimpleHttpResponse;
import org.xbib.service.client.invocation.RemoteInvokerFactory;

import java.net.URI;

import static org.junit.Assert.assertEquals;

public class HttpSessionTest {

    @Test
    public void testGet() throws Exception {
        RemoteInvokerFactory remoteInvokerFactory = RemoteInvokerFactory.DEFAULT;
        SimpleHttpClient client = Clients.newClient(remoteInvokerFactory, "none+http://www.google.com",
                SimpleHttpClient.class);
        SimpleHttpRequest request = SimpleHttpRequestBuilder.forGet("/search?q=köln")
                .header(HttpHeaderNames.ACCEPT, "utf-8")
                .build();
        SimpleHttpResponse response = client.execute(request).get();
        // --> www.google.de
        int max = 3;
        while (response.followUrl() != null && max-- > 0) {
            URI uri = URI.create(response.followUrl());
            client = Clients.newClient(remoteInvokerFactory, "none+" + uri,
                    SimpleHttpClient.class);
            request = SimpleHttpRequestBuilder.forGet(uri.getPath())
                    .header(HttpHeaderNames.ACCEPT, "utf-8")
                    .build();
            response = client.execute(request).get();
        }
        assertEquals(HttpResponseStatus.OK, response.status());
    }

    @Test
    public void testPost() throws Exception {
        RemoteInvokerFactory remoteInvokerFactory = RemoteInvokerFactory.DEFAULT;
        SimpleHttpClient client = Clients.newClient(remoteInvokerFactory, "none+http://www.google.com",
                SimpleHttpClient.class);
        SimpleHttpRequest request = SimpleHttpRequestBuilder.forPost("/search")
                .header(HttpHeaderNames.ACCEPT, "utf-8")
                .header(HttpHeaderNames.CONTENT_LENGTH, "0")
                .build();
        SimpleHttpResponse response = client.execute(request).get();
        assertEquals(HttpResponseStatus.METHOD_NOT_ALLOWED, response.status());
    }
}

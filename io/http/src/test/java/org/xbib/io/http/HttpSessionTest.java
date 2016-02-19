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

import org.junit.Test;
import org.xbib.io.Request;
import org.xbib.io.Session;
import org.xbib.io.http.netty.NettyHttpResponseListener;
import org.xbib.io.http.netty.NettyHttpSession;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertTrue;

public class HttpSessionTest {

    @Test
    public void testGet() throws Exception {
        NettyHttpSession session = new NettyHttpSession();
        session.open(Session.Mode.READ);
        HttpRequest request = session.newRequest()
                .setMethod("GET")
                .setURL(new URL("http://www.google.com/search"))
                .addParameter("q", "köln");
        AtomicInteger counter = new AtomicInteger();
        request.prepare().execute(new NettyHttpResponseListener() {
            @Override
            public void receivedResponse(HttpResponse result) {
                //logger.info("result = {}", result);
                counter.incrementAndGet();
            }
            @Override
            public void onError(Request request, Throwable error) throws IOException {
                //logger.error(error.getMessage(), error);
            }
        }).waitFor(15L, TimeUnit.SECONDS);
        session.close();
        assertTrue(counter.get() > 0);
    }

    @Test
    public void testPost() throws Exception {
        NettyHttpSession session = new NettyHttpSession();
        session.open(Session.Mode.READ);
        HttpRequest request = session.newRequest()
                .setMethod("POST")
                .setURL(new URL("http://www.google.com/search"))
                .addHeader("Content-Length", "0")
                .addParameter("q", "köln");
        AtomicInteger counter = new AtomicInteger();
        request.prepare().execute(new NettyHttpResponseListener() {
            @Override
            public void receivedResponse(HttpResponse result) {
                //logger.info("result = {}",result);
                counter.incrementAndGet();
            }
        }).waitFor(15, TimeUnit.SECONDS);
        session.close();
        assertTrue(counter.get() > 0);
    }
}

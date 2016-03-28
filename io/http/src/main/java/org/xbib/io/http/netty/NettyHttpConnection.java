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
package org.xbib.io.http.netty;

import org.xbib.io.Connection;
import org.xbib.io.http.HttpRequest;
import org.xbib.io.http.HttpResponse;
import org.xbib.io.http.HttpSession;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownServiceException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class NettyHttpConnection extends URLConnection implements Connection<HttpSession> {

    private HttpSession session;

    private NettyHttpResponse nettyHttpResponse;

    private Throwable throwable;

    /**
     * Constructs a URL connection to the specified URL. A connection to
     * the object referenced by the URL is not created.
     *
     * @param url the specified URL.
     */
    protected NettyHttpConnection(URL url) throws URISyntaxException {
        super(url);
    }

    @Override
    public void connect() throws IOException {
        this.session = createSession();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if (session == null) {
            connect();
        }
        try {
            HttpRequest request = session.newRequest().setMethod("GET").setURL(url);
            NettyHttpResponseListener listener = new NettyHttpResponseListener() {
                @Override
                public void receivedResponse(HttpResponse result) {
                    setNettyHttpResponse((NettyHttpResponse) result);
                }
                @Override
                public void onError(HttpRequest request, Throwable error) throws IOException {
                    setThrowable(error);
                }
            };
            request.prepare().execute(listener).waitFor(15L, TimeUnit.SECONDS);
            return nettyHttpResponse != null ? nettyHttpResponse.getResponseBodyAsStream() : null;
        } catch (URISyntaxException | ExecutionException | TimeoutException | InterruptedException e) {
            throw new IOException(e);
        }
    }

    void setNettyHttpResponse(NettyHttpResponse nettyHttpResponse) {
        this.nettyHttpResponse = nettyHttpResponse;
    }

    void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        throw new UnknownServiceException("protocol doesn't support output");
    }

    @Override
    public HttpSession createSession() throws IOException {
        return new NettyHttpSession();
    }

    @Override
    public void close() throws IOException {
        if (session != null) {
            session.close();
        }
    }

}

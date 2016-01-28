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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.io.http.HttpPacket;
import org.xbib.io.http.HttpRequest;
import org.xbib.io.http.PreparedHttpRequest;
import org.xbib.io.http.client.Realm;
import org.xbib.io.http.client.Request;
import org.xbib.io.http.client.RequestBuilder;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class NettyHttpRequest extends HttpPacket implements HttpRequest {

    private final static Logger logger = LogManager.getLogger(NettyHttpRequest.class.getName());

    private final NettyHttpSession session;

    private String method = "GET";

    private URL url;

    private RequestBuilder requestBuilder;

    private Realm.Builder realmBuilder;

    private Request request;

    protected NettyHttpRequest(NettyHttpSession session) {
        this.session = session;
    }

    @Override
    public String getQuery() {
        return url != null ? url.toString() : null;
    }

    @Override
    public URL getURL() {
        return url;
    }

    @Override
    public NettyHttpRequest setURL(URL url) throws URISyntaxException, MalformedURLException {
        if (url.getUserInfo() != null) {
            String[] userInfo = url.getUserInfo().split(":");
            this.realmBuilder = new Realm.Builder(userInfo[0], userInfo[1]);
            realmBuilder = realmBuilder.setUsePreemptiveAuth(true).setScheme(Realm.AuthScheme.BASIC);
        }
        String authority = url.getHost() + (url.getPort() > 0 ? ":" + url.getPort() : "");
        // add authority, drop all query parameters (use the RequestBuilder for that)
        URI uri = new URI(url.toURI().getScheme(), authority, url.getPath(), null, url.toURI().getFragment());
        this.url = uri.toURL();
        return this;
    }

    public String getMethod() {
        return method;
    }

    @Override
    public NettyHttpRequest setMethod(String method) {
        this.method = method;
        this.requestBuilder = new RequestBuilder(method, true); // true = use raw URL
        return this;
    }

    @Override
    public NettyHttpRequest addParameter(String name, String value) {
        if (value != null && value.length() > 0 && requestBuilder != null) {
            requestBuilder.addQueryParam(name, value);
        }
        return this;
    }

    @Override
    public NettyHttpRequest addHeader(String name, String value) {
        if (value != null && value.length() > 0 && requestBuilder != null) {
            requestBuilder.addHeader(name, value);
        }
        return this;
    }

    @Override
    public NettyHttpRequest setBody(String body) {
        if (requestBuilder != null) {
            requestBuilder.setBody(body);
        }
        return this;
    }

    @Override
    public PreparedHttpRequest prepare() throws IOException {
        if (url == null) {
            throw new IOException("no URL set");
        }
        if (request == null) {
            if (requestBuilder == null) {
                if (method == null) {
                    this.method = "GET";
                }
                this.requestBuilder = new RequestBuilder(method, true);
            }
            requestBuilder = requestBuilder.setUrl(url.toString());
            if (realmBuilder != null) {
                requestBuilder.setRealm(realmBuilder.build());
            }
            this.request = requestBuilder.build();
            logger.debug("prepared " + toString());
        }
        return session.prepare(this);
    }

    protected Request getRequest() {
        return request;
    }

    public String toString() {
        return "[method=" + method + "]" +
                "[url=" + url + "]" +
                "[parameter=" + requestBuilder
                .setUrl(url.toString())
                .setRealm(realmBuilder != null ? realmBuilder.build() : null)
                .build() + "]";
    }

}

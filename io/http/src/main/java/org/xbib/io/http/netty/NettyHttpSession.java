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

import io.netty.handler.ssl.OpenSsl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.io.http.HttpPacket;
import org.xbib.io.http.HttpRequest;
import org.xbib.io.http.HttpSession;
import org.xbib.io.http.PreparedHttpRequest;
import org.xbib.io.http.client.AsyncHttpClient;
import org.xbib.io.http.client.DefaultAsyncHttpClient;
import org.xbib.io.http.client.DefaultAsyncHttpClientConfig;
import org.xbib.io.http.client.proxy.ProxyServer;

import java.io.IOException;

public class NettyHttpSession implements HttpSession {

    private final static Logger logger = LogManager.getLogger(NettyHttpSession.class);

    private DefaultAsyncHttpClientConfig.Builder config = new DefaultAsyncHttpClientConfig.Builder();

    private AsyncHttpClient client;

    private boolean isOpen;

    protected PreparedHttpRequest prepare(NettyHttpRequest request) throws IOException {
        if (!isOpen()) {
            open(Mode.READ);
        }
        return new NettyPreparedHttpRequest(request, client.prepareRequest(request.getRequest()));
    }

    public DefaultAsyncHttpClientConfig.Builder getConfig() {
        return config;
    }

    @Override
    public HttpSession setProxy(String host, int port) {
        if (host == null) {
            return this;
        }
        config.setProxyServer(new ProxyServer(host, port, 0, null, null, false));
        return this;
    }

    @Override
    public HttpSession setTimeout(int millis) {
        if (millis <= 0) {
            return this;
        }
        config.setRequestTimeout(millis);
        return this;
    }

    @Override
    public HttpRequest newRequest() {
        return new NettyHttpRequest(this);
    }

    @Override
    public void open(Mode mode) throws IOException {
        open(mode, 120 * 1000);
    }

    public synchronized void open(Mode mode, int timeout) throws IOException {
        if (!isOpen) {
            switch (mode) {
                case READ: {
                    // some reasonable defaults for web browsing
                    config.setUseOpenSsl(true)
                            .setAcceptAnyCertificate(true)
                            .setFollowRedirect(true)
                            //.setAllowPoolingConnection(true)
                            //.setAllowSslConnectionPool(true)
                            //.setMaximumConnectionsPerHost(16)
                            //.setMaximumConnectionsTotal(16)
                            //.setFollowRedirects(true)
                            .setMaxRequestRetry(3) // for slow DNB OAI
                            //.setUseRawUrl(true) // do not auto-encode HTTP GET params
                            .setCompressionEnforced(true)
                            //.setCompressionEnabled(true)
                            //.setConnectionTimeoutInMs(timeout)
                            .setConnectTimeout(timeout)
                            .setReadTimeout(timeout)
                            .setPooledConnectionIdleTimeout(timeout);
                    //.setRequestTimeoutInMs(timeout)
                    //.setIdleConnectionTimeoutInMs(timeout)
                    //.setIdleConnectionInPoolTimeoutInMs(timeout)

                    break;
                }
                case CONTROL: {
                    // for crawling
                    logger.info("OpenSSL available = {}", OpenSsl.isAvailable());
                    logger.info("ALPN supported = {}", OpenSsl.isAlpnSupported());
                    config.setUseOpenSsl(true)
                            .setAcceptAnyCertificate(true)
                            .setFollowRedirect(false)
                            //setAllowPoolingConnection(true)
                            //setAllowSslConnectionPool(true)
                            //.setMaximumConnectionsPerHost(16)
                            //.setMaximumConnectionsTotal(16)
                            //.setFollowRedirects(false) // do not follow HTTP code 302
                            .setMaxRequestRetry(3) // for slow DNB OAI
                            //.setUseRawUrl(true) // do not auto-encode HTTP GET params
                            //.setCompressionEnabled(true)
                            .setConnectTimeout(timeout)
                            .setReadTimeout(timeout)
                            .setPooledConnectionIdleTimeout(timeout);
                    //.setConnectionTimeoutInMs(timeout)
                    //.setRequestTimeoutInMs(timeout)
                    //.setIdleConnectionTimeoutInMs(timeout)
                    //.setIdleConnectionInPoolTimeoutInMs(timeout);
                    break;
                }
            }
            this.client = new DefaultAsyncHttpClient(config.build());
            //new AsyncHttpClient(new NettyAsyncHttpProvider(config.build()));
            this.isOpen = true;
        }
    }

    @Override
    public synchronized void close() throws IOException {
        if (isOpen) {
            this.isOpen = false;
            client.close();
        }
    }

    @Override
    public boolean isOpen() {
        return isOpen;
    }

    @Override
    public HttpPacket newPacket() {
        return null;
    }

    @Override
    public HttpPacket read() throws IOException {
        return null;
    }

    @Override
    public void write(HttpPacket packet) throws IOException {
    }




}

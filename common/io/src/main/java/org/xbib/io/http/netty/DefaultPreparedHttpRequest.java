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

import com.ning.http.client.AsyncHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.HttpResponseBodyPart;
import com.ning.http.client.HttpResponseHeaders;
import com.ning.http.client.HttpResponseStatus;
import com.ning.http.client.Request;
import org.xbib.io.http.HttpFuture;
import org.xbib.io.http.HttpRequest;
import org.xbib.io.http.HttpResponse;
import org.xbib.io.http.HttpResponseListener;
import org.xbib.io.http.PreparedHttpRequest;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;

import java.io.IOException;

/**
 *
 */
public class DefaultPreparedHttpRequest implements PreparedHttpRequest {

    private final Logger logger = LoggerFactory.getLogger(DefaultPreparedHttpRequest.class.getName());

    private final HttpRequest request;

    private final AsyncHttpClient.BoundRequestBuilder bound;

    private String encoding = System.getProperty("file.encoding");

    DefaultPreparedHttpRequest(HttpRequest request, AsyncHttpClient.BoundRequestBuilder bound) {
        this.request = request;
        this.bound = bound;
    }

    @Override
    public DefaultPreparedHttpRequest setEncoding(String encoding) {
        this.encoding = encoding;
        return this;
    }

    public String getEncoding() {
        return encoding;
    }

    @Override
    public HttpFuture execute() throws IOException {
        Request r = bound.build();
        logger.debug("executing raw URL {}", r.getRawUrl());
        return new DefaultHttpFuture(bound.execute());
    }

    @Override
    public HttpFuture execute(HttpResponseListener listener) throws IOException {
        Request r = bound.build();
        logger.debug("executing raw URL {}", r.getRawUrl());
        return new DefaultHttpFuture(bound.execute(new Handler(listener)));
    }

    class Handler implements AsyncHandler<HttpResponse> {

        private final HttpResponseListener listener;

        private final DefaultHttpResponse result;

        private final StringBuilder sb;

        Handler(HttpResponseListener listener) {
            this.listener = listener;
            this.result = new DefaultHttpResponse();
            this.sb = new StringBuilder();
        }

        @Override
        public STATE onStatusReceived(HttpResponseStatus hrs) throws Exception {
            result.setStatusCode(hrs.getStatusCode());
            return STATE.CONTINUE;
        }

        @Override
        public STATE onHeadersReceived(HttpResponseHeaders hrh) throws Exception {
            result.setHeaders(hrh.getHeaders());
            return STATE.CONTINUE;
        }

        @Override
        public STATE onBodyPartReceived(HttpResponseBodyPart hrbp) throws Exception {
            String s = new String(hrbp.getBodyPartBytes(), encoding);
            sb.append(s);
            if (listener != null) {
                listener.onReceive(request, s);
            }
            return STATE.CONTINUE;
        }

        @Override
        public DefaultHttpResponse onCompleted() throws Exception {
            result.setBody(sb.toString());
            if (listener != null) {
                try {
                    listener.receivedResponse(result);
                } catch (IOException e) {
                    onThrowable(e);
                }
            }
            return result;
        }

        @Override
        public void onThrowable(Throwable t) {
            logger.error(t.getMessage(), t);
            result.setThrowable(t);
            if (listener != null) {
                try {
                    listener.onError(request, t.toString());
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }

    }

}

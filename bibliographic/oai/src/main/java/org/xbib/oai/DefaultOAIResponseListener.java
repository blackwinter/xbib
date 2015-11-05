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
package org.xbib.oai;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.io.Request;
import org.xbib.io.http.netty.NettyHttpResponseListener;
import org.xbib.io.http.HttpResponse;
import org.xbib.oai.client.ClientOAIResponse;

import java.io.IOException;

public class DefaultOAIResponseListener<Response extends OAIResponse>
        extends NettyHttpResponseListener implements OAIResponseListener {

    private final Logger logger = LogManager.getLogger(DefaultOAIResponseListener.class.getName());

    private OAIRequest request;

    private Response response;

    private StringBuilder sb;

    private boolean failure;

    public DefaultOAIResponseListener() {
        this(null);
    }

    protected DefaultOAIResponseListener(OAIRequest request) {
        this.request = request;
        this.sb = new StringBuilder();
        this.failure = false;
    }

    public boolean isFailure() {
        return failure;
    }

    public Response getResponse() {
        return response;
    }

    @Override
    public void receivedResponse(HttpResponse result) throws IOException {
        super.receivedResponse(result);
        this.response = (Response)new ClientOAIResponse();
        if (!result.ok()) {
            String msg = "HTTP error " + result.getStatusCode();
            if (result.getThrowable() == null) {
                Throwable t = new IOException(msg);
                result.setThrowable(t);
                throw new IOException(t);
            }
            return;
        }
        if (result.getThrowable() != null) {
            throw new IOException(result.getThrowable());
        }
        logger.debug("got response: status = {}, headers = {}, body = {}",
                result.getStatusCode(),
                result.getHeaders(),
                sb.toString()
        );
        if (!result.getContentType().endsWith("xml")) {
            logger.warn("got non-XML body {}", result);
        }
        //response.setReader(getBodyReader());
    }

    @Override
    public void onConnect(Request request) throws IOException {
    }

    @Override
    public void onDisconnect(Request request) throws IOException {
    }

    @Override
    public void onReceive(Request request, CharSequence message) throws IOException {
       sb.append(message);
    }

    @Override
    public void onError(Request request, CharSequence errorMessage) throws IOException {
        logger.error("received error {}", errorMessage);
        this.failure = true;
    }

}

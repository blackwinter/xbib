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
package org.xbib.oai.client.listrecords;

import java.io.IOException;
import java.io.StringReader;
import java.util.Date;
import java.util.List;

import org.xbib.io.Request;
import org.xbib.io.http.netty.NettyHttpResponseListener;
import org.xbib.oai.OAIResponseListener;
import org.xbib.oai.util.ResumptionToken;
import org.xbib.util.DateUtil;
import org.xbib.io.http.HttpResponse;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;

import org.xbib.xml.XMLUtil;
import org.xbib.xml.transform.StylesheetTransformer;
import org.xml.sax.InputSource;

public class ListRecordsListener extends NettyHttpResponseListener
        implements OAIResponseListener {

    private final Logger logger = LoggerFactory.getLogger(ListRecordsListener.class.getName());

    private final ListRecordsRequest request;

    private final ListRecordsResponse response;

    private ListRecordsFilterReader filterreader;

    private StringBuilder body;

    private boolean scrubCharacters;

    public ListRecordsListener(ListRecordsRequest request) {
        this.request = request;
        this.response = new ListRecordsResponse(request);
        this.body = new StringBuilder();
    }

    public ListRecordsListener setScrubCharacters(boolean scrub) {
        this.scrubCharacters = scrub;
        return this;
    }

    @Override
    public ListRecordsResponse getResponse() {
        return response;
    }

    @Override
    public void receivedResponse(HttpResponse result) throws IOException {
        super.receivedResponse(result);
        int status = result.getStatusCode();
        if (status == 503) {
            logger.warn("got 503 status: headers={} body={}", result.getHeaders(), body);
            doRetryAfter(result);
            return;
        }
        if (!result.ok()) {
            throw new IOException("status  = " + status + " response = " + body, result.getThrowable());
        }
        // activate XSLT only if XML content type
        if (result.getContentType().endsWith("xml")) {
            StylesheetTransformer transformer = new StylesheetTransformer("xsl");
            this.filterreader = new ListRecordsFilterReader(request, response);
            String s = !scrubCharacters ? body.toString() : XMLUtil.clean(body.toString());
            InputSource source = new InputSource(new StringReader(s));
            transformer.setSource(filterreader, source);
            response.setTransformer(transformer);
        } else {
            throw new IOException("no XML content type in response");
        }
    }

    private final static String[] RETRY_AFTER = new String[] {
      "retry-after", "Retry-after", "Retry-After"
    };

    private void doRetryAfter(HttpResponse httpResponse) {
        if (httpResponse.getHeaders() == null) {
            return;
        }
        for (String retryAfterHeader : RETRY_AFTER) {
            List<String> retryAfterValues = httpResponse.getHeaders().get(retryAfterHeader);
            if (retryAfterValues == null) {
                continue;
            }
            if (retryAfterValues.size() < 1) {
                continue;
            }
            String retryAfter = retryAfterValues.get(0);
            if (isDigits(retryAfter)) {
                // retry-after is given in seconds
                response.setExpire(Long.parseLong(retryAfter));
                request.setRetry(true);
            } else {
                Date d = DateUtil.parseDateRFC(retryAfter);
                if (d != null) {
                    response.setExpire(d.getTime() - new Date().getTime());
                    request.setRetry(true);
                }
            }
        }
    }

    private boolean isDigits(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }


    @Override
    public void onConnect(Request request) throws IOException {
    }

    @Override
    public void onDisconnect(Request request) throws IOException {
    }

    @Override
    public void onReceive(Request request, CharSequence message) throws IOException {
        body.append(message);
    }

    @Override
    public void onError(Request request, CharSequence errorMessage) throws IOException {
        logger.error("received error {}", errorMessage);
    }

    public ResumptionToken getResumptionToken() {
        return filterreader != null ? filterreader.getResumptionToken() : null;
    }

}

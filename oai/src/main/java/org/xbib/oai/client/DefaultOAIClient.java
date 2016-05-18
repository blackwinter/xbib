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
package org.xbib.oai.client;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Duration;

import org.xbib.oai.client.getrecord.GetRecordRequest;
import org.xbib.oai.client.identify.IdentifyRequest;
import org.xbib.oai.client.listidentifiers.ListIdentifiersRequest;
import org.xbib.oai.client.listmetadataformats.ListMetadataFormatsRequest;
import org.xbib.oai.client.listrecords.ListRecordsRequest;
import org.xbib.oai.client.listsets.ListSetsRequest;
import org.xbib.oai.util.ResumptionToken;
import org.xbib.service.client.ClientBuilder;
import org.xbib.service.client.http.SimpleHttpClient;

/**
 * Default OAI client
 */
public class DefaultOAIClient implements OAIClient {

    private SimpleHttpClient client;

    private URL url;

    @Override
    public DefaultOAIClient setURL(URL url) throws URISyntaxException {
        this.url = url;
        this.client = new ClientBuilder("none+" + url.toURI())
                .responseTimeout(Duration.ofMinutes(1L)) // maybe not enough for extreme slow archive servers...
                .build(SimpleHttpClient.class);
        return this;
    }

    @Override
    public URL getURL() {
        return url;
    }

    @Override
    public SimpleHttpClient getHttpClient() {
        return client;
    }

    @Override
    public IdentifyRequest newIdentifyRequest() {
        IdentifyRequest request = new IdentifyRequest();
        request.setURL(url);
        return request;
    }

    @Override
    public ListMetadataFormatsRequest newListMetadataFormatsRequest() {
        ListMetadataFormatsRequest request = new ListMetadataFormatsRequest();
        request.setURL(getURL());
        return request;
    }

    @Override
    public ListSetsRequest newListSetsRequest() {
        ListSetsRequest request = new ListSetsRequest();
        request.setURL(getURL());
        return request;
    }

    @Override
    public ListIdentifiersRequest newListIdentifiersRequest() {
        ListIdentifiersRequest request = new ListIdentifiersRequest();
        request.setURL(getURL());
        return request;
    }

    @Override
    public GetRecordRequest newGetRecordRequest() {
        GetRecordRequest request = new GetRecordRequest();
        request.setURL(getURL());
        return request;
    }

    @Override
    public ListRecordsRequest newListRecordsRequest() {
        ListRecordsRequest request = new ListRecordsRequest();
        request.setURL(getURL());
        return request;
    }

    @Override
    public IdentifyRequest resume(IdentifyRequest request, ResumptionToken token) {
        if (request.isRetry()) {
            request.setRetry(false);
            return request;
        }
        if (token == null) {
            return null;
        }
        request = newIdentifyRequest();
        request.setResumptionToken(token);
        return request;
    }

    @Override
    public ListRecordsRequest resume(ListRecordsRequest request, ResumptionToken token) {
        if (request.isRetry()) {
            request.setRetry(false);
            return request;
        }
        if (token == null) {
            return null;
        }
        request = newListRecordsRequest();
        request.setResumptionToken(token);
        return request;
    }

    @Override
    public ListIdentifiersRequest resume(ListIdentifiersRequest request, ResumptionToken token) {
        if (request.isRetry()) {
            request.setRetry(false);
            return request;
        }
        if (token == null) {
            return null;
        }
        request = newListIdentifiersRequest();
        request.setResumptionToken(token);
        return request;
    }

    @Override
    public ListMetadataFormatsRequest resume(ListMetadataFormatsRequest request, ResumptionToken token) {
        if (request.isRetry()) {
            request.setRetry(false);
            return request;
        }
        if (token == null) {
            return null;
        }
        request = newListMetadataFormatsRequest();
        request.setResumptionToken(token);
        return request;
    }

    @Override
    public ListSetsRequest resume(ListSetsRequest request, ResumptionToken token) {
        if (request.isRetry()) {
            request.setRetry(false);
            return request;
        }
        if (token == null) {
            return null;
        }
        request = newListSetsRequest();
        request.setResumptionToken(token);
        return request;
    }

    @Override
    public GetRecordRequest resume(GetRecordRequest request, ResumptionToken token) {
        if (request.isRetry()) {
            request.setRetry(false);
            return request;
        }
        if (token == null) {
            return null;
        }
        request = newGetRecordRequest();
        request.setResumptionToken(token);
        return request;
    }

    @Override
    public void close() throws IOException {

    }
}

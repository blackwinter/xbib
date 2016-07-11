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

import org.xbib.oai.OAIConstants;
import org.xbib.oai.OAIRequest;
import org.xbib.oai.util.ResumptionToken;
import org.xbib.service.client.http.SimpleHttpRequest;
import org.xbib.service.client.http.SimpleHttpRequestBuilder;
import org.xbib.util.URIBuilder;
import org.xbib.util.URIFormatter;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * Client OAI request
 */
public class ClientOAIRequest<R extends ClientOAIRequest> implements OAIRequest<R> {

    private SimpleHttpRequestBuilder builder;

    private URIBuilder uriBuilder;

    private DateTimeFormatter dateTimeFormatter;

    private ResumptionToken token;

    private String set;

    private String metadataPrefix;

    private Instant from;

    private Instant until;

    private boolean retry;

    protected ClientOAIRequest() {
        uriBuilder = new URIBuilder();
    }

    public R setURL(URL url) {
        try {
            URI uri = url.toURI();
            uriBuilder.scheme(uri.getScheme())
                    .authority(uri.getAuthority())
                    .path(uri.getPath());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("invalid URI " + url);
        }
        return (R)this;
    }

    public URL getURL() throws MalformedURLException {
        return uriBuilder.build().toURL();
    }

    public SimpleHttpRequest getHttpRequest() {
        return builder.forGet(uriBuilder.build().toString()).build();
    }

    public R addParameter(String name, String value) {
        uriBuilder.addParameter(name, value);
        return (R) this;
    }

    @Override
    public R setSet(String set) {
        this.set = set;
        addParameter(OAIConstants.SET_PARAMETER, set);
        return (R) this;
    }

    public String getSet() {
        return set;
    }

    @Override
    public R setMetadataPrefix(String prefix) {
        this.metadataPrefix = prefix;
        addParameter(OAIConstants.METADATA_PREFIX_PARAMETER, prefix);
        return (R) this;
    }

    public String getMetadataPrefix() {
        return metadataPrefix;
    }

    public R setDateTimeFormatter(DateTimeFormatter dateTimeFormatter) {
        this.dateTimeFormatter = dateTimeFormatter;
        return (R)this;
    }

    @Override
    public R setFrom(Instant from) {
        this.from = from;
        String fromStr = dateTimeFormatter == null ? from.toString() : dateTimeFormatter.format(from);
        addParameter(OAIConstants.FROM_PARAMETER, fromStr);
        return (R) this;
    }

    public Instant getFrom() {
        return from;
    }

    @Override
    public R setUntil(Instant until) {
        this.until = until;
        String untilStr = dateTimeFormatter == null ? until.toString() : dateTimeFormatter.format(until);
        addParameter(OAIConstants.UNTIL_PARAMETER, untilStr);
        return (R) this;
    }

    public Instant getUntil() {
        return until;
    }

    public R setResumptionToken(ResumptionToken token) {
        this.token = token;
        if (token != null && token.toString() != null) {
            // resumption token may have characters that are illegal in URIs like '|'
            //String tokenStr = URIFormatter.encode(token.toString(), StandardCharsets.UTF_8);
            addParameter(OAIConstants.RESUMPTION_TOKEN_PARAMETER, token.toString());
        }
        return (R) this;
    }

    public ResumptionToken getResumptionToken() {
        return token;
    }

    public R setRetry(boolean retry) {
        this.retry = retry;
        return (R) this;
    }

    public boolean isRetry() {
        return retry;
    }

    class GetRecord extends ClientOAIRequest<GetRecord> {

        public GetRecord() {
            addParameter(VERB_PARAMETER, GET_RECORD);
        }
    }

    class Identify extends ClientOAIRequest<Identify> {

        public Identify() {
            addParameter(VERB_PARAMETER, IDENTIFY);
        }
    }

    class ListIdentifiers extends ClientOAIRequest<ListIdentifiers> {

        public ListIdentifiers() {
            addParameter(VERB_PARAMETER, LIST_IDENTIFIERS);
        }
    }

    class ListMetadataFormats extends ClientOAIRequest<ListMetadataFormats> {

        public ListMetadataFormats() {
            addParameter(VERB_PARAMETER, LIST_METADATA_FORMATS);
        }
    }

    class ListRecordsRequest extends ClientOAIRequest<ListRecordsRequest> {

        public ListRecordsRequest() {
            addParameter(OAIConstants.VERB_PARAMETER, LIST_RECORDS);
        }

    }

    class ListSetsRequest extends ClientOAIRequest<ListSetsRequest> {

        public ListSetsRequest() {
            addParameter(VERB_PARAMETER, LIST_SETS);
        }
    }
}
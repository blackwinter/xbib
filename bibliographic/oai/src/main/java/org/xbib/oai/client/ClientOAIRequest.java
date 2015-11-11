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

import org.xbib.io.http.HttpRequest;
import org.xbib.oai.OAIConstants;
import org.xbib.oai.OAIDateResolution;
import org.xbib.oai.OAIRequest;
import org.xbib.oai.OAISession;
import org.xbib.util.DateUtil;
import org.xbib.io.http.netty.NettyHttpRequest;
import org.xbib.oai.util.ResumptionToken;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;

/**
 * Client OAI request
 */
public class ClientOAIRequest<R extends ClientOAIRequest>
        extends NettyHttpRequest implements HttpRequest, OAIRequest<R> {

    //private final static Charset UTF8 = Charset.forName("UTF-8");

    private ResumptionToken token;

    private String set;

    private String metadataPrefix;

    private Date from;

    private Date until;

    private boolean retry;

    protected ClientOAIRequest(OAISession session) {
        super(session.getSession());
        setMethod("GET");
        addHeader("User-Agent", OAIClient.USER_AGENT);
    }

    @Override
    public R setUser(String user) {
        super.setUser(user);
        return (R) this;
    }

    @Override
    public R setPassword(String password) {
        super.setPassword(password);
        return (R) this;
    }

    @Override
    public R setURL(URI uri) throws URISyntaxException {
        super.setURL(uri);
        return (R) this;
    }

    @Override
    public R addParameter(String name, String value) {
        if (value != null && value.length() > 0) {
            super.addParameter(name, value);
        }
        return (R) this;
    }

    public R setSet(String set) {
        this.set = set;
        addParameter(OAIConstants.SET_PARAMETER, set);
        return (R) this;
    }

    public String getSet() {
        return set;
    }

    public R setMetadataPrefix(String prefix) {
        this.metadataPrefix = prefix;
        addParameter(OAIConstants.METADATA_PREFIX_PARAMETER, prefix);
        return (R) this;
    }

    public String getMetadataPrefix() {
        return metadataPrefix;
    }

    public R setFrom(Date from, OAIDateResolution resolution) {
        this.from = from;
        addParameter(OAIConstants.FROM_PARAMETER, DateUtil.formatDate(from,
                resolution == OAIDateResolution.DAY ? DateUtil.ISO_FORMAT_DAYS :
                        DateUtil.ISO_FORMAT_SECONDS
        ));
        return (R) this;
    }

    public Date getFrom() {
        return from;
    }

    public R setUntil(Date until, OAIDateResolution resolution) {
        this.until = until;
        addParameter(OAIConstants.UNTIL_PARAMETER, DateUtil.formatDate(until,
                resolution == OAIDateResolution.DAY ? DateUtil.ISO_FORMAT_DAYS :
                        DateUtil.ISO_FORMAT_SECONDS
        ));
        return (R) this;
    }

    public Date getUntil() {
        return until;
    }

    public R setResumptionToken(ResumptionToken token) {
        this.token = token;
        if (token != null) {
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

        public GetRecord(OAISession session) {
            super(session);
            addParameter(VERB_PARAMETER, GET_RECORD);
        }
    }

    class Identify extends ClientOAIRequest<Identify> {

        public Identify(OAISession session) {
            super(session);
            addParameter(VERB_PARAMETER, IDENTIFY);
        }
    }

    class ListIdentifiers extends ClientOAIRequest<ListIdentifiers> {

        public ListIdentifiers(OAISession session) {
            super(session);
            addParameter(VERB_PARAMETER, LIST_IDENTIFIERS);
        }
    }

    class ListMetadataFormats extends ClientOAIRequest<ListMetadataFormats> {

        public ListMetadataFormats(OAISession session) {
            super(session);
            addParameter(VERB_PARAMETER, LIST_METADATA_FORMATS);
        }
    }

    class ListRecordsRequest extends ClientOAIRequest<ListRecordsRequest> {

        public ListRecordsRequest(OAISession session) {
            super(session);
            addParameter(OAIConstants.VERB_PARAMETER, LIST_RECORDS);
        }

    }

    class ListSetsRequest extends ClientOAIRequest<ListSetsRequest> {

        public ListSetsRequest(OAISession session) {
            super(session);
            addParameter(VERB_PARAMETER, LIST_SETS);
        }
    }
}
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

import io.netty.handler.codec.http.HttpHeaders;
import org.xbib.io.http.HttpResponse;
import org.xbib.io.http.client.HttpResponseBodyPart;
import org.xbib.io.http.client.HttpResponseHeaders;
import org.xbib.io.http.client.HttpResponseStatus;
import org.xbib.io.http.client.netty.NettyResponse;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class NettyHttpResponse extends NettyResponse implements HttpResponse {

    public NettyHttpResponse(HttpResponseStatus status,
            HttpResponseHeaders headers,
            List<HttpResponseBodyPart> bodyParts) {
        super(status, headers, bodyParts);
    }

    public Map<String, List<String>> getHeaderMap() {
        HttpHeaders headers = getHeaders();
        Map<String, List<String>> map = new LinkedHashMap<>();
        for (String key : headers.names()) {
            List<String> values = headers.getAll(key);
            map.put(key, values);
        }
        return map;
    }

    @Override
    public boolean ok() {
        return getStatusCode() == 200;
    }

    @Override
    public boolean forbidden() {
        return getStatusCode() == 403;
    }

    @Override
    public boolean notfound() {
        return getStatusCode() == 404;
    }

    @Override
    public boolean fatal() {
        return (getStatusCode() >= 500 && getStatusCode() < 600);
    }
}

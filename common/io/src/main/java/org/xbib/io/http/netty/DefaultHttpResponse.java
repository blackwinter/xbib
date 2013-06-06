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

import com.ning.http.client.FluentCaseInsensitiveStringsMap;
import org.xbib.io.http.HttpPacket;
import org.xbib.io.http.HttpResponse;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DefaultHttpResponse extends HttpPacket implements HttpResponse {

    private URI uri;

    private int statusCode;

    private FluentCaseInsensitiveStringsMap headers;

    private String body;

    private Throwable throwable;

    public DefaultHttpResponse setURI(URI uri) {
        this.uri = uri;
        return this;
    }
    
    public URI getURI() {
        return uri;
    }
    
    public DefaultHttpResponse setStatusCode(int code) {
        this.statusCode = code;
        return this;
    }

    @Override
    public int getStatusCode() {
        return statusCode;
    }
    
    public DefaultHttpResponse setHeaders(FluentCaseInsensitiveStringsMap headers) {
        this.headers = headers;
        return this;
    }
    
    public Map<String,List<String>> getHeaders() {
        Map<String,List<String>> map = new LinkedHashMap();
        for (String key : headers.keySet()) {
            List<String> values = headers.get(key);
            map.put(key, values);
        }
        return map;
    }
    
    public DefaultHttpResponse setBody(String body) {
        this.body = body;
        return this;
    }

    @Override
    public String getBody() {
        return body;
    }

    @Override
    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    @Override
    public Throwable getThrowable() {
        return throwable;
    }

    @Override
    public boolean ok() {
       return getStatusCode() == 200 && getThrowable() == null;
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
        return (getStatusCode() >= 500 && getStatusCode() < 600) || getThrowable() != null;
    }

}
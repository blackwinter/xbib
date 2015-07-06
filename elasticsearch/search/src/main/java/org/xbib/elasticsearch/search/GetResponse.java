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
package org.xbib.elasticsearch.search;

import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.json.JsonXContent;

import org.xbib.elasticsearch.action.search.support.BasicGetResponse;
import org.xbib.elasticsearch.xml.ES;
import org.xbib.io.stream.Streams;
import org.xbib.io.stream.StreamByteBuffer;
import org.xbib.json.xml.JsonXmlStreamer;
import org.xbib.json.transform.JsonStylesheet;
import org.xbib.search.SearchError;
import org.xbib.xml.transform.StylesheetTransformer;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.util.XMLEventConsumer;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Writer;

public class GetResponse extends BasicGetResponse {

    private StylesheetTransformer transformer;

    private String[] stylesheets;

    private String mimeType;

    public GetResponse setOutputFormat(String mimeType) {
        this.mimeType = mimeType;
        return this;
    }

    public String getOutputFormat() {
        return mimeType;
    }

    public GetResponse setStylesheetTransformer(StylesheetTransformer transformer) {
        this.transformer = transformer;
        return this;
    }

    public GetResponse setStylesheets(String... stylesheets) {
        this.stylesheets = stylesheets;
        return this;
    }

    protected StylesheetTransformer getTransformer() {
        return transformer;
    }

    protected String[] getStylesheets() {
        return stylesheets;
    }

    private final QName root = new QName(ES.NS_URI, "result", ES.NS_PREFIX);

    public void to(XMLEventConsumer consumer) throws IOException, XMLStreamException {
        new JsonXmlStreamer().root(root).toXML(bytes().getInputStream(), consumer);
    }

    public void to(Writer writer) throws IOException, XMLStreamException {
        InputStream in = bytes().getInputStream();
        if (getStylesheets() == null || "application/json".equals(mimeType)) {
            Streams.copy(new InputStreamReader(in, "UTF-8"), writer);
        } else if ("application/xml".equals(mimeType)) {
            new JsonStylesheet().root(root)
                    .toXML(in, writer);
        } else {
            new JsonStylesheet().root(root)
                    .setTransformer(getTransformer())
                    .setStylesheets(getStylesheets())
                    .transform(in, writer);
        }
        writer.flush();
    }

    private void check() throws IOException {
        if (getResponse() == null) {
            throw new IOException("no response");
        }
        final boolean error = !getResponse().isExists();
        // error handling
        if (error) {
            throw new SearchError("document does not exist");
        }
    }

    private StreamByteBuffer bytes() throws IOException {
        check();
        StreamByteBuffer buffer = new StreamByteBuffer();
        OutputStream out = buffer.getOutputStream();
        XContentBuilder jsonBuilder = new XContentBuilder(JsonXContent.jsonXContent, out);
        getResponse().toXContent(jsonBuilder, ToXContent.EMPTY_PARAMS);
        jsonBuilder.close();
        out.flush();
        return buffer;
    }

}

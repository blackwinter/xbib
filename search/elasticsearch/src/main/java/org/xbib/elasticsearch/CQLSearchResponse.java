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
package org.xbib.elasticsearch;

import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.ShardSearchFailure;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.json.JsonXContent;

import org.xbib.elasticsearch.xml.ES;
import org.xbib.io.stream.ChunkedStream;
import org.xbib.io.StreamUtil;
import org.xbib.io.stream.StreamByteBuffer;
import org.xbib.json.JsonXmlStreamer;
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

/**
 * Response for a CQL search
 *
 */
public class CQLSearchResponse {

    private SearchResponse searchResponse;

    private GetResponse getResponse;

    private StylesheetTransformer transformer;

    private String[] stylesheets;

    private String mimeType;

    public CQLSearchResponse setOutputFormat(String mimeType) {
        this.mimeType = mimeType;
        return this;
    }

    public String getOutputFormat() {
        return mimeType;
    }

    public CQLSearchResponse setStylesheetTransformer(StylesheetTransformer transformer) {
        this.transformer = transformer;
        return this;
    }

    public CQLSearchResponse setStylesheets(String... stylesheets) {
        this.stylesheets = stylesheets;
        return this;
    }

    protected StylesheetTransformer getTransformer() {
        return transformer;
    }

    protected String[] getStylesheets() {
        return stylesheets;
    }

    public CQLSearchResponse setSearchResponse(SearchResponse response) {
        this.searchResponse = response;
        return this;
    }

    public SearchResponse getSearchResponse() {
        return searchResponse;
    }

    public CQLSearchResponse setGetResponse(GetResponse response) {
        this.getResponse = response;
        return this;
    }

    public GetResponse getGetResponse() {
        return getResponse;
    }

    public long tookInMillis() {
        return searchResponse.getTookInMillis();
    }

    public long totalHits() {
        return searchResponse.getHits().getTotalHits();
    }

    public boolean exists() {
        return getResponse.isExists();
    }

    public void toJSON(OutputStream out) throws IOException {
        if (out == null) {
            return;
        }
        if (searchResponse == null) {
            return;
        }
        checkResponseForError();
        XContentBuilder jsonBuilder = new XContentBuilder(JsonXContent.jsonXContent, out);
        jsonBuilder.startObject();
        searchResponse.toXContent(jsonBuilder, ToXContent.EMPTY_PARAMS);
        jsonBuilder.endObject();
        jsonBuilder.close();
    }

    private final QName root = new QName(ES.NS_URI, "result", ES.NS_PREFIX);

    public void to(XMLEventConsumer consumer) throws IOException, XMLStreamException {
        JsonXmlStreamer streamer = new JsonXmlStreamer().root(root);
        streamer.toXML(read(), consumer);
    }

    public InputStream read() throws IOException {
        if (searchResponse == null) {
            return null;
        }
        checkResponseForError();
        StreamByteBuffer buffer = new StreamByteBuffer();
        toJSON(buffer.getOutputStream());
        buffer.getOutputStream().flush();
        return buffer.getInputStream();
    }

    public StreamByteBuffer bytes() throws IOException {
        if (searchResponse == null) {
            return null;
        }
        checkResponseForError();
        StreamByteBuffer buffer = new StreamByteBuffer();
        toJSON(buffer.getOutputStream());
        buffer.getOutputStream().flush();
        return buffer;
    }

    public void to(Writer writer) throws IOException, XMLStreamException {
        InputStream in = read();
        if (getStylesheets() == null || "application/json".equals(mimeType)) {
            StreamUtil.copy(new InputStreamReader(in, "UTF-8"), writer);
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

    private void checkResponseForError() throws IOException {
        final boolean error = searchResponse.getFailedShards() > 0 || searchResponse.isTimedOut();
        // error handling
        if (error) {
            StringBuilder sb = new StringBuilder();
            if (searchResponse.getFailedShards() > 0) {
                for (ShardSearchFailure shf : searchResponse.getShardFailures()) {
                    sb.append(Integer.toString(shf.shardId())).append("=").append(shf.reason()).append(" ");
                }
            }
            throw new SearchError(sb.toString());
        }
    }

}

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
package org.xbib.sru.searchretrieve;

import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.util.XMLEventConsumer;
import javax.xml.transform.TransformerException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.io.Request;
import org.xbib.io.http.HttpResponse;
import org.xbib.io.http.HttpResponseListener;
import org.xbib.sru.DefaultSRUResponse;
import org.xbib.sru.SRUResponse;
import org.xbib.sru.SRUVersion;
import java.text.Normalizer;
import org.xbib.xml.transform.StylesheetTransformer;
import org.xml.sax.InputSource;

/**
 * SearchRetrieve response
 *
 */
public class SearchRetrieveResponse extends DefaultSRUResponse
        implements SRUResponse, SearchRetrieveListener, HttpResponseListener, XMLEventConsumer {

    private final static Logger logger = LogManager.getLogger(SearchRetrieveResponse.class.getName());

    private final SearchRetrieveRequest request;

    private HttpResponse httpResponse;

    private StringBuilder sb;

    public SearchRetrieveResponse(SearchRetrieveRequest request) {
        this.request = request;
        this.sb = new StringBuilder();
    }

    @Override
    public void receivedResponse(HttpResponse response) {
        this.httpResponse = response;
    }

    public int httpStatus() {
        return httpResponse.getStatusCode();
    }

    @Override
    public void onConnect(Request request) throws IOException {
        for (SearchRetrieveListener listener : this.request.getListeners()) {
            listener.onConnect(request);
        }
    }

    @Override
    public void onDisconnect(Request request) throws IOException {
        for (SearchRetrieveListener listener : this.request.getListeners()) {
            listener.onDisconnect(request);
        }
    }

    @Override
    public void onError(Request request, Throwable error) throws IOException {
        for (SearchRetrieveListener listener : this.request.getListeners()) {
            listener.onError(request, error);
        }
    }

    @Override
    public void onReceive(Request request, CharSequence message) throws IOException {
        sb.append(message);
        for (SearchRetrieveListener listener : this.request.getListeners()) {
            listener.onReceive(request, message);
        }
    }

    @Override
    public void version(String version) {
        for (SearchRetrieveListener listener : this.request.getListeners()) {
            listener.version(version);
        }
    }

    @Override
    public void numberOfRecords(long numberOfRecords) {
        for (SearchRetrieveListener listener : this.request.getListeners()) {
            listener.numberOfRecords(numberOfRecords);
        }
    }
    
    @Override
    public void beginRecord() {
        for (SearchRetrieveListener listener : this.request.getListeners()) {
            listener.beginRecord();
        }
    }

    @Override
    public void recordSchema(String recordSchema) {
        for (SearchRetrieveListener listener : this.request.getListeners()) {
            listener.recordSchema(recordSchema);
        }
    }

    @Override
    public void recordPacking(String recordPacking) {
        for (SearchRetrieveListener listener : this.request.getListeners()) {
            listener.recordPacking(recordPacking);
        }
    }

    @Override
    public void recordIdentifier(String recordIdentifier) {
        for (SearchRetrieveListener listener : this.request.getListeners()) {
            listener.recordIdentifier(recordIdentifier);
        }
    }
    @Override
    public void recordPosition(int recordPosition) {
        for (SearchRetrieveListener listener : this.request.getListeners()) {
            listener.recordPosition(recordPosition);
        }
    }

    @Override
    public XMLEventConsumer recordData() {
        return null;
    }


    @Override
    public XMLEventConsumer extraRecordData() {
        return null;
    }

    @Override
    public void facetedResults(Collection<XMLEvent> record) {
        for (SearchRetrieveListener listener : this.request.getListeners()) {
            listener.facetedResults(record);
        }
    }

    @Override
    public void endRecord() {
        for (SearchRetrieveListener listener : this.request.getListeners()) {
            listener.endRecord();
        }
    }

    @Override
    public void add(XMLEvent event) throws XMLStreamException {
        for (SearchRetrieveListener listener : this.request.getListeners()) {
            XMLEventConsumer consumer = listener.recordData();
            if (consumer != null) {
                consumer.add(event);
            }
        }
    }

    public boolean isEmpty() {
        return httpResponse != null && httpResponse.notfound();
    }

    @Override
    public SearchRetrieveResponse to(Writer writer) throws IOException {
        if (httpResponse == null) {
            return this;
        }
        if (getTransformer() == null) {
            setStylesheetTransformer(new StylesheetTransformer().setPath("xsl"));
        }
        // transport parameters into XSL transformer style sheets
        getTransformer().addParameter("version", request.getVersion());
        getTransformer().addParameter("operation", "searchRetrieve");
        getTransformer().addParameter("query", request.getQuery());
        getTransformer().addParameter("startRecord", request.getStartRecord());
        getTransformer().addParameter("maximumRecords", request.getMaximumRecords());
        getTransformer().addParameter("recordPacking", request.getRecordPacking());
        getTransformer().addParameter("recordSchema", request.getRecordSchema());

        try {
            SearchRetrieveFilterReader reader = new SearchRetrieveFilterReader(request);
            for (SearchRetrieveListener listener : this.request.getListeners()) {
                reader.addRecordDataConsumer(listener.recordData());
                reader.addExtraRecordDataConsumer(listener.extraRecordData());
            }
            // TODO normalization should be configurable
            String s = sb.toString();
            InputSource source = new InputSource(new StringReader(Normalizer.normalize(s, Normalizer.Form.NFC)));
            getTransformer().setSource(reader, source).setResult(writer);
            SRUVersion version = SRUVersion.fromString(request.getVersion());
            String[] stylesheets = getStylesheets(version);
            if (stylesheets != null) {
                getTransformer().transform(Arrays.asList(stylesheets));
            } else {
                getTransformer().transform();
            }
        } catch (TransformerException e) {
            logger.error(e.getMessage(), e);
            throw new IOException(e);
        } finally {
            writer.flush();
            getTransformer().close();
        }
        return this;
    }

}


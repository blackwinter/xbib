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
package org.xbib.io.iso23950.searchretrieve;

import org.xbib.sru.SRUVersion;
import org.xbib.sru.searchretrieve.SearchRetrieveRequest;
import org.xbib.sru.SRUFilterReader;
import org.xbib.io.iso23950.ZResponse;
import org.xbib.marc.Iso2709Reader;
import org.xbib.sru.searchretrieve.SearchRetrieveResponse;
import org.xml.sax.InputSource;

import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.util.Arrays;

public class ZSearchRetrieveResponse extends SearchRetrieveResponse
        implements ZResponse {

    private SearchRetrieveRequest request;

    private byte[] records;

    private String format;

    private String type;

    private long resultCount;

    public ZSearchRetrieveResponse(SearchRetrieveRequest request) {
        super(request);
        this.request = request;
    }

    public ZSearchRetrieveResponse setRecords(byte[] records) {
        this.records = records;
        return this;
    }

    public ZSearchRetrieveResponse setErrors(byte[] errors) {
        return this;
    }

    public ZSearchRetrieveResponse setFormat(String format) {
        this.format = format;
        return this;
    }

    public ZSearchRetrieveResponse setType(String type) {
        this.type = type;
        return this;
    }

    public ZSearchRetrieveResponse setResultCount(long count) {
        this.resultCount = count;
        return this;
    }

    @Override
    public ZSearchRetrieveResponse to(Writer writer) throws IOException {
        // get result count for caller and for stylesheet
        numberOfRecords(resultCount);
        if (getTransformer() == null) {
            return this;
        }
        getTransformer().addParameter("numberOfRecords", resultCount);
        // push out results
        ByteArrayInputStream in = new ByteArrayInputStream(records);
        // stream encoding, must always be octet!
        Reader reader = new InputStreamReader(in, "ISO-8859-1");
        SRUFilterReader sruFilterReader = new SRUFilterReader(this, reader, "UTF-8");
        try {
            sruFilterReader.setProperty(Iso2709Reader.FORMAT, format);
            sruFilterReader.setProperty(Iso2709Reader.TYPE, type);
            StreamResult streamResult = new StreamResult(writer);
            getTransformer().setSource(new SAXSource(sruFilterReader, new InputSource(reader))).setResult(streamResult);
            SRUVersion version = SRUVersion.fromString(request.getVersion());
            if (getStylesheets(version) != null) {
                getTransformer().transform(Arrays.asList(getStylesheets(version)));
            }
        } catch (Exception e) {
            throw new IOException(e);
        } finally {
            writer.flush();
        }
        return this;
    }

}


